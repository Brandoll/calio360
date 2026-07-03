package com.calio.identity.service.impl;

import com.calio.identity.dto.request.*;
import com.calio.identity.dto.response.*;
import com.calio.identity.entity.*;
import com.calio.identity.exception.*;
import com.calio.identity.messaging.UserEventPublisher;
import com.calio.identity.repository.*;
import com.calio.identity.security.JwtUtils;
import com.calio.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service @RequiredArgsConstructor @Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final UserSettingsRepository settingsRepo;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher eventPublisher;

    @Value("${calio.jwt.refresh-expiration-ms}") private long refreshExpirationMs;

    @Override @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new ConflictException("El correo ya está registrado: " + req.getEmail());

        User user = User.builder()
            .email(req.getEmail().toLowerCase())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .birthDate(req.getBirthDate())
            .gender(req.getGender() != null ? User.Gender.valueOf(req.getGender()) : null)
            .build();
        user = userRepo.save(user);

        // Configuración por defecto
        settingsRepo.save(UserSettings.builder().user(user).build());

        String accessToken = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user);

        eventPublisher.publishUserRegistered(user.getId(), user.getEmail(), user.getFirstName());
        log.info("Usuario registrado: {}", user.getEmail());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail().toLowerCase())
            .orElseThrow(() -> new ResourceNotFoundException("Credenciales inválidas"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash()))
            throw new ResourceNotFoundException("Credenciales inválidas");
        if (!user.getActive())
            throw new ConflictException("Cuenta desactivada");

        String accessToken = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user);
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override @Transactional
    public AuthResponse refreshToken(String token) {
        RefreshToken rt = refreshTokenRepo.findByTokenAndRevokedFalse(token)
            .orElseThrow(() -> new ResourceNotFoundException("Refresh token inválido o expirado"));
        if (rt.getExpiresAt().isBefore(OffsetDateTime.now())) {
            rt.setRevoked(true);
            refreshTokenRepo.save(rt);
            throw new ConflictException("Refresh token expirado");
        }
        User user = rt.getUser();
        String newAccess = jwtUtils.generateToken(user.getId(), user.getEmail());
        String newRefresh = createRefreshToken(user);
        rt.setRevoked(true);
        refreshTokenRepo.save(rt);
        return buildAuthResponse(newAccess, newRefresh, user);
    }

    @Override @Transactional
    public void logout(Long userId) {
        refreshTokenRepo.revokeAllByUserId(userId);
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        refreshTokenRepo.save(RefreshToken.builder()
            .user(user)
            .token(token)
            .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000))
            .build());
        return token;
    }

    private AuthResponse buildAuthResponse(String access, String refresh, User user) {
        return AuthResponse.builder()
            .accessToken(access)
            .refreshToken(refresh)
            .tokenType("Bearer")
            .expiresIn(86400L)
            .user(UserResponse.builder()
                .id(user.getId()).email(user.getEmail())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .createdAt(user.getCreatedAt()).active(user.getActive())
                .emailVerified(user.getEmailVerified()).build())
            .build();
    }
}
