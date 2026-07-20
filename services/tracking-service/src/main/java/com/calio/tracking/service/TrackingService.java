package com.calio.tracking.service;

import com.calio.tracking.dto.request.MealRequest;
import com.calio.tracking.dto.request.WaterRequest;
import com.calio.tracking.dto.response.DailySummaryResponse;
import com.calio.tracking.dto.response.MealRecordDto;
import com.calio.tracking.messaging.TrackingEventPublisher;
import com.calio.tracking.model.AguaDiaria;
import com.calio.tracking.model.ComidaRegistrada;
import com.calio.tracking.repository.AguaDiariaRepository;
import com.calio.tracking.repository.ComidaRegistradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final ComidaRegistradaRepository comidaRepository;
    private final AguaDiariaRepository aguaRepository;
    private final TrackingEventPublisher eventPublisher;

    @Transactional
    public Map<String, Object> registrarComida(MealRequest request) {
        ComidaRegistrada comida = new ComidaRegistrada();
        comida.setUserId(request.getUserId());
        comida.setAlimentoId(request.getAlimentoId());
        comida.setNombre(request.getNombre());
        comida.setPorcionGramos(request.getPorcionGramos());
        comida.setMomento(request.getMomento());
        comida.setFecha(request.getFecha());
        comida.setCalorias(request.getCalorias());
        comida.setProteinas(request.getProteinas());
        comida.setGrasas(request.getGrasas());
        comida.setCarbohidratos(request.getCarbohidratos());
        comida.setImageUrl(request.getImageUrl());

        comidaRepository.save(comida);
        eventPublisher.publishComidaRegistrada(comida.getUserId(), comida.getFecha().toString(), comida.getCalorias());

        return Map.of("dailyCalories", calcularCaloriasDia(comida.getUserId(), comida.getFecha()));
    }

    @Transactional
    public Map<String, Object> registrarAgua(WaterRequest request) {
        AguaDiaria agua = aguaRepository.findByUserIdAndFecha(request.getUserId(), request.getFecha())
                .orElse(new AguaDiaria(request.getUserId(), request.getFecha(), 0));
        
        agua.setVasos(agua.getVasos() + request.getVasos());
        aguaRepository.save(agua);
        eventPublisher.publishAguaActualizada(agua.getUserId(), agua.getFecha().toString(), agua.getVasos());

        return Map.of("aguaVasos", agua.getVasos());
    }

    public DailySummaryResponse getResumenDiario(Long userId, LocalDate fecha) {
        List<ComidaRegistrada> comidas = comidaRepository.findByUserIdAndFecha(userId, fecha);
        
        int totalCal = comidas.stream().mapToInt(ComidaRegistrada::getCalorias).sum();
        BigDecimal totalProt = comidas.stream().map(ComidaRegistrada::getProteinas).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGrasas = comidas.stream().map(ComidaRegistrada::getGrasas).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCarbs = comidas.stream().map(ComidaRegistrada::getCarbohidratos).reduce(BigDecimal.ZERO, BigDecimal::add);

        int vasos = aguaRepository.findByUserIdAndFecha(userId, fecha).map(AguaDiaria::getVasos).orElse(0);

        List<MealRecordDto> comidasDto = comidas.stream().map(c -> new MealRecordDto(
                c.getId(),
                c.getUserId(),
                c.getAlimentoId(),
                c.getNombre(),
                c.getPorcionGramos(),
                c.getMomento(),
                c.getImageUrl(),
                c.getFecha(),
                c.getCalorias(),
                c.getProteinas(),
                c.getGrasas(),
                c.getCarbohidratos()
        )).collect(Collectors.toList());

        return new DailySummaryResponse(totalCal, totalProt, totalGrasas, totalCarbs, vasos, comidasDto);
    }

    private int calcularCaloriasDia(Long userId, LocalDate fecha) {
        return comidaRepository.findByUserIdAndFecha(userId, fecha).stream().mapToInt(ComidaRegistrada::getCalorias).sum();
    }

    @Transactional
    public void deleteMeal(Long mealId) {
        comidaRepository.findById(mealId).ifPresent(comida -> {
            comidaRepository.delete(comida);
            eventPublisher.publishComidaRegistrada(comida.getUserId(), comida.getFecha().toString(), -comida.getCalorias());
        });
    }
}
