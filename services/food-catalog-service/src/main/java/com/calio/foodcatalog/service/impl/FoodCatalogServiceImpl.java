package com.calio.foodcatalog.service.impl;

import com.calio.foodcatalog.dto.response.*;
import com.calio.foodcatalog.entity.*;
import com.calio.foodcatalog.exception.*;
import com.calio.foodcatalog.repository.*;
import com.calio.foodcatalog.service.FoodCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class FoodCatalogServiceImpl implements FoodCatalogService {

    private final AlimentoRepository alimentoRepo;
    private final CategoriaRepository categoriaRepo;
    private final UserFavoriteRepository favRepo;

    @Override
    public List<CategoriaResponse> getAllCategorias() {
        return categoriaRepo.findAll().stream()
            .map(c -> CategoriaResponse.builder()
                .id(c.getId()).codigoLetra(c.getCodigoLetra()).nombre(c.getNombre())
                .totalAlimentos(alimentoRepo.findByCategoriaId(c.getId(), Pageable.unpaged()).getTotalElements())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public AlimentoResponse getAlimentoById(Integer id, Long userId) {
        Alimento a = alimentoRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alimento no encontrado: " + id));
        return toResponse(a, userId);
    }

    @Override
    public AlimentoResponse getAlimentoByCodigo(String codigo, Long userId) {
        Alimento a = alimentoRepo.findByCodigoIns(codigo.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Código no encontrado: " + codigo));
        return toResponse(a, userId);
    }

    @Override
    public PagedResponse<AlimentoResponse> searchAlimentos(String query, Integer categoriaId, Long userId, Pageable pageable) {
        Page<Alimento> page;
        if (query != null && !query.isBlank() && categoriaId != null) {
            page = alimentoRepo.searchByNombreAndCategoria(query, categoriaId, pageable);
        } else if (query != null && !query.isBlank()) {
            page = alimentoRepo.searchByNombre(query, pageable);
        } else if (categoriaId != null) {
            page = alimentoRepo.findByCategoriaId(categoriaId, pageable);
        } else {
            page = alimentoRepo.findAll(pageable);
        }
        return toPagedResponse(page, userId);
    }

    @Override
    public PagedResponse<AlimentoResponse> getAlimentosByCategoria(Integer categoriaId, Long userId, Pageable pageable) {
        if (!categoriaRepo.existsById(categoriaId))
            throw new ResourceNotFoundException("Categoría no encontrada: " + categoriaId);
        return toPagedResponse(alimentoRepo.findByCategoriaId(categoriaId, pageable), userId);
    }

    @Override @Transactional
    public AlimentoResponse addFavorito(Long userId, Integer alimentoId) {
        if (favRepo.existsByUserIdAndAlimentoId(userId, alimentoId))
            throw new ConflictException("El alimento ya está en favoritos");
        Alimento a = alimentoRepo.findById(alimentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Alimento no encontrado: " + alimentoId));
        favRepo.save(UserFavorite.builder().userId(userId).alimento(a).build());
        return toResponse(a, userId);
    }

    @Override @Transactional
    public void removeFavorito(Long userId, Integer alimentoId) {
        if (!favRepo.existsByUserIdAndAlimentoId(userId, alimentoId))
            throw new ResourceNotFoundException("Favorito no encontrado");
        favRepo.deleteByUserIdAndAlimentoId(userId, alimentoId);
    }

    @Override
    public List<AlimentoResponse> getFavoritos(Long userId) {
        return favRepo.findByUserId(userId).stream()
            .map(f -> toResponse(f.getAlimento(), userId))
            .collect(Collectors.toList());
    }

    private Set<Integer> getUserFavIds(Long userId) {
        if (userId == null) return Collections.emptySet();
        return favRepo.findByUserId(userId).stream()
            .map(f -> f.getAlimento().getId()).collect(Collectors.toSet());
    }

    private AlimentoResponse toResponse(Alimento a, Long userId) {
        boolean esFav = userId != null && favRepo.existsByUserIdAndAlimentoId(userId, a.getId());
        return AlimentoResponse.builder()
            .id(a.getId()).codigoIns(a.getCodigoIns()).nombre(a.getNombre())
            .porcionRef(a.getPorcionRef())
            .categoria(a.getCategoria() != null ? a.getCategoria().getNombre() : null)
            .energiaKcal(a.getEnergiaKcal()).proteinasG(a.getProteinasG())
            .grasaTotalG(a.getGrasaTotalG()).carbohidratosTotalesG(a.getCarbohidratosTotalesG())
            .fibraDietariaG(a.getFibraDietariaG()).calcioMg(a.getCalcioMg())
            .hierroMg(a.getHierroMg()).vitaminaCMg(a.getVitaminaCMg())
            .esFavorito(esFav).build();
    }

    private PagedResponse<AlimentoResponse> toPagedResponse(Page<Alimento> page, Long userId) {
        Set<Integer> favIds = getUserFavIds(userId);
        List<AlimentoResponse> content = page.getContent().stream()
            .map(a -> {
                AlimentoResponse r = toResponse(a, null);
                r.setEsFavorito(favIds.contains(a.getId()));
                return r;
            }).collect(Collectors.toList());
        return PagedResponse.<AlimentoResponse>builder()
            .content(content).page(page.getNumber()).size(page.getSize())
            .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
            .last(page.isLast()).build();
    }
}
