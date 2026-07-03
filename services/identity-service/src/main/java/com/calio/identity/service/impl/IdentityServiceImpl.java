package com.calio.identity.service.impl;

import com.calio.identity.dto.request.*;
import com.calio.identity.dto.response.*;
import com.calio.identity.entity.*;
import com.calio.identity.exception.*;
import com.calio.identity.messaging.UserEventPublisher;
import com.calio.identity.repository.*;
import com.calio.identity.service.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j
public class IdentityServiceImpl implements IdentityService {

    private final UserRepository userRepo;
    private final BiometricRecordRepository biometricRepo;
    private final UserGoalRepository goalRepo;
    private final UserSettingsRepository settingsRepo;
    private final UserEventPublisher eventPublisher;

    @Override
    public UserResponse getProfile(Long userId) {
        return toUserResponse(findUser(userId));
    }

    @Override @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = findUser(userId);
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null) user.setLastName(req.getLastName());
        if (req.getBirthDate() != null) user.setBirthDate(req.getBirthDate());
        if (req.getGender() != null) user.setGender(User.Gender.valueOf(req.getGender()));
        user = userRepo.save(user);

        settingsRepo.findByUserId(userId).ifPresent(s -> {
            if (req.getUnitSystem() != null) s.setUnitSystem(req.getUnitSystem());
            if (req.getLanguage() != null) s.setLanguage(req.getLanguage());
            if (req.getNotificationsEnabled() != null) s.setNotificationsEnabled(req.getNotificationsEnabled());
            settingsRepo.save(s);
        });

        eventPublisher.publishProfileUpdated(userId);
        return toUserResponse(user);
    }

    @Override @Transactional
    public void deleteAccount(Long userId) {
        User user = findUser(userId);
        user.setActive(false);
        userRepo.save(user);
    }

    @Override @Transactional
    public BiometricResponse addBiometricRecord(Long userId, BiometricRequest req) {
        User user = findUser(userId);
        BiometricRecord record = BiometricRecord.builder()
            .user(user)
            .weightKg(req.getWeightKg())
            .heightCm(req.getHeightCm())
            .bodyFatPct(req.getBodyFatPct())
            .build();
        record = biometricRepo.save(record);
        log.info("Registro biométrico guardado para userId={}", userId);

        // Si tiene objetivo activo, recalcular calorías automáticamente
        goalRepo.findActiveGoalByUserId(userId).ifPresent(goal -> {
            int calorias = calcularCaloriasHarrisBenedict(
                req.getWeightKg(), req.getHeightCm(),
                user.getBirthDate(), user.getGender(),
                goal.getActivityLevel(), goal.getGoalType()
            );
            goal.setDailyCalories(calorias);
            goal.setProteinGrams(calcularProteinas(req.getWeightKg(), goal.getGoalType()));
            goal.setFatGrams(calcularGrasas(calorias));
            goal.setCarbsGrams(calcularCarbos(calorias, goal.getProteinGrams(), goal.getFatGrams()));
            goalRepo.save(goal);
            log.info("Macros recalculados para userId={}: {} kcal", userId, calorias);
        });

        return toBiometricResponse(record);
    }

    @Override
    public List<BiometricResponse> getBiometricHistory(Long userId) {
        findUser(userId);
        return biometricRepo.findByUserIdOrderByRecordedAtDesc(userId, PageRequest.of(0, 30))
            .stream().map(this::toBiometricResponse).toList();
    }

    @Override @Transactional
    public GoalResponse setGoal(Long userId, GoalRequest req) {
        User user = findUser(userId);

        // Cerrar objetivo anterior
        goalRepo.findActiveGoalByUserId(userId).ifPresent(g -> {
            g.setEndDate(LocalDate.now());
            goalRepo.save(g);
        });

        // Calcular calorías automáticamente si hay biometría
        int dailyCalories = req.getDailyCalories() != null ? req.getDailyCalories() : 2000;
        BigDecimal proteinGrams = req.getProteinGrams();
        BigDecimal fatGrams = req.getFatGrams();
        BigDecimal carbsGrams = req.getCarbsGrams();

        List<BiometricRecord> historial = biometricRepo
            .findByUserIdOrderByRecordedAtDesc(userId, PageRequest.of(0, 1));

        if (!historial.isEmpty()) {
            BiometricRecord ultimo = historial.get(0);
            UserGoal.GoalType goalType = UserGoal.GoalType.valueOf(req.getGoalType());
            UserGoal.ActivityLevel activityLevel = UserGoal.ActivityLevel.valueOf(req.getActivityLevel());

            dailyCalories = calcularCaloriasHarrisBenedict(
                ultimo.getWeightKg(), ultimo.getHeightCm(),
                user.getBirthDate(), user.getGender(),
                activityLevel, goalType
            );
            proteinGrams = calcularProteinas(ultimo.getWeightKg(), goalType);
            fatGrams = calcularGrasas(dailyCalories);
            carbsGrams = calcularCarbos(dailyCalories, proteinGrams, fatGrams);
            log.info("Calorías calculadas automáticamente para userId={}: {} kcal", userId, dailyCalories);
        }

        UserGoal goal = UserGoal.builder()
            .user(user)
            .goalType(UserGoal.GoalType.valueOf(req.getGoalType()))
            .activityLevel(UserGoal.ActivityLevel.valueOf(req.getActivityLevel()))
            .targetWeightKg(req.getTargetWeightKg())
            .dailyCalories(dailyCalories)
            .proteinGrams(proteinGrams)
            .fatGrams(fatGrams)
            .carbsGrams(carbsGrams)
            .build();

        return toGoalResponse(goalRepo.save(goal));
    }

    @Override
    public GoalResponse getActiveGoal(Long userId) {
        findUser(userId);
        return toGoalResponse(
            goalRepo.findActiveGoalByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No hay objetivo activo para el usuario " + userId))
        );
    }

    @Override
    public SettingsResponse getSettings(Long userId) {
        findUser(userId);
        UserSettings s = settingsRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada"));
        return toSettingsResponse(s);
    }

    @Override @Transactional
    public SettingsResponse updateSettings(Long userId, UpdateProfileRequest req) {
        findUser(userId);
        UserSettings s = settingsRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada"));
        if (req.getUnitSystem() != null) s.setUnitSystem(req.getUnitSystem());
        if (req.getLanguage() != null) s.setLanguage(req.getLanguage());
        if (req.getNotificationsEnabled() != null) s.setNotificationsEnabled(req.getNotificationsEnabled());
        return toSettingsResponse(settingsRepo.save(s));
    }

    // ─── Fórmula Harris-Benedict ──────────────────────────────────────────────

    private int calcularCaloriasHarrisBenedict(
            BigDecimal weightKg, BigDecimal heightCm,
            LocalDate birthDate, User.Gender gender,
            UserGoal.ActivityLevel activityLevel, UserGoal.GoalType goalType) {

        if (weightKg == null || heightCm == null) return 2000;

        double peso = weightKg.doubleValue();
        double talla = heightCm.doubleValue();
        int edad = birthDate != null ? Period.between(birthDate, LocalDate.now()).getYears() : 25;

        // TMB (Tasa Metabólica Basal)
        double tmb;
        if (gender == User.Gender.FEMALE) {
            tmb = 447.593 + (9.247 * peso) + (3.098 * talla) - (4.330 * edad);
        } else {
            tmb = 88.362 + (13.397 * peso) + (4.799 * talla) - (5.677 * edad);
        }

        // Factor de actividad
        double factor = switch (activityLevel) {
            case SEDENTARY   -> 1.2;
            case LIGHT       -> 1.375;
            case MODERATE    -> 1.55;
            case ACTIVE      -> 1.725;
            case VERY_ACTIVE -> 1.9;
        };

        double tdee = tmb * factor;

        // Ajuste según objetivo
        double ajuste = switch (goalType) {
            case LOSE_WEIGHT  -> -500.0;  // déficit de 500 kcal
            case GAIN_MUSCLE  -> +300.0;  // superávit de 300 kcal
            case MAINTAIN, EAT_HEALTHY -> 0.0;
        };

        return (int) Math.round(tdee + ajuste);
    }

    private BigDecimal calcularProteinas(BigDecimal weightKg, UserGoal.GoalType goalType) {
        if (weightKg == null) return BigDecimal.valueOf(150);
        double grPorKg = goalType == UserGoal.GoalType.GAIN_MUSCLE ? 2.0 : 1.6;
        return BigDecimal.valueOf(weightKg.doubleValue() * grPorKg).setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularGrasas(int totalCalorias) {
        // 25% de las calorías totales en grasa (9 kcal/g)
        return BigDecimal.valueOf((totalCalorias * 0.25) / 9).setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularCarbos(int totalCalorias, BigDecimal proteinas, BigDecimal grasas) {
        // Resto de calorías tras proteínas (4 kcal/g) y grasas (9 kcal/g)
        double calProteinas = proteinas != null ? proteinas.doubleValue() * 4 : 0;
        double calGrasas    = grasas != null    ? grasas.doubleValue() * 9    : 0;
        double calCarbos    = totalCalorias - calProteinas - calGrasas;
        return BigDecimal.valueOf(calCarbos / 4).setScale(1, RoundingMode.HALF_UP);
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────

    private User findUser(Long id) {
        return userRepo.findById(id)
            .filter(User::getActive)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private UserResponse toUserResponse(User u) {
        return UserResponse.builder()
            .id(u.getId()).email(u.getEmail())
            .firstName(u.getFirstName()).lastName(u.getLastName())
            .birthDate(u.getBirthDate())
            .gender(u.getGender() != null ? u.getGender().name() : null)
            .createdAt(u.getCreatedAt()).active(u.getActive())
            .emailVerified(u.getEmailVerified()).build();
    }

    private BiometricResponse toBiometricResponse(BiometricRecord r) {
        BigDecimal bmi = null;
        if (r.getWeightKg() != null && r.getHeightCm() != null) {
            BigDecimal hm = r.getHeightCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            bmi = r.getWeightKg().divide(hm.multiply(hm), 2, RoundingMode.HALF_UP);
        }
        return BiometricResponse.builder().id(r.getId())
            .weightKg(r.getWeightKg()).heightCm(r.getHeightCm())
            .bodyFatPct(r.getBodyFatPct()).bmi(bmi)
            .recordedAt(r.getRecordedAt()).build();
    }

    private GoalResponse toGoalResponse(UserGoal g) {
        return GoalResponse.builder().id(g.getId())
            .goalType(g.getGoalType() != null ? g.getGoalType().name() : null)
            .activityLevel(g.getActivityLevel() != null ? g.getActivityLevel().name() : null)
            .targetWeightKg(g.getTargetWeightKg())
            .dailyCalories(g.getDailyCalories())
            .proteinGrams(g.getProteinGrams())
            .carbsGrams(g.getCarbsGrams())
            .fatGrams(g.getFatGrams())
            .startDate(g.getStartDate())
            .createdAt(g.getCreatedAt()).build();
    }

    private SettingsResponse toSettingsResponse(UserSettings s) {
        return SettingsResponse.builder()
            .unitSystem(s.getUnitSystem())
            .language(s.getLanguage())
            .notificationsEnabled(s.getNotificationsEnabled()).build();
    }
}
