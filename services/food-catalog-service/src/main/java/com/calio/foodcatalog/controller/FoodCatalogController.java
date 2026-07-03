package com.calio.foodcatalog.controller;

import com.calio.foodcatalog.dto.response.*;
import com.calio.foodcatalog.service.FoodCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/catalogo")
@RequiredArgsConstructor
public class FoodCatalogController {

    private final FoodCatalogService catalogService;

    private Long getUserId(Object principal) {
        if (principal == null) return null;
        try { return Long.parseLong(principal.toString()); } catch (Exception e) { return null; }
    }

    /** RF-22: Listar categorías */
    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> getCategorias() {
        return ResponseEntity.ok(ApiResponse.ok("Categorías disponibles", catalogService.getAllCategorias()));
    }

    /**
     * RF-21: Búsqueda por nombre
     * RF-22: Filtrado por categoría
     * RF-23: Ver alimentos locales
     * RF-26: Alimentos con alta densidad nutricional
     * GET /api/v1/catalogo/alimentos?q=quinua&categoriaId=8&page=0&size=20
     */
    @GetMapping("/alimentos")
    public ResponseEntity<ApiResponse<PagedResponse<AlimentoResponse>>> searchAlimentos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @AuthenticationPrincipal Object principal) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(sortBy));
        return ResponseEntity.ok(ApiResponse.ok("Resultados de búsqueda",
            catalogService.searchAlimentos(q, categoriaId, getUserId(principal), pageable)));
    }

    /** RF-24: Tabla nutricional por ID */
    @GetMapping("/alimentos/{id}")
    public ResponseEntity<ApiResponse<AlimentoResponse>> getAlimentoById(
            @PathVariable Integer id,
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(ApiResponse.ok("Alimento encontrado",
            catalogService.getAlimentoById(id, getUserId(principal))));
    }

    /** Buscar por código INS (ej: A001) */
    @GetMapping("/alimentos/codigo/{codigo}")
    public ResponseEntity<ApiResponse<AlimentoResponse>> getAlimentoByCodigo(
            @PathVariable String codigo,
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(ApiResponse.ok("Alimento encontrado",
            catalogService.getAlimentoByCodigo(codigo, getUserId(principal))));
    }

    /** Alimentos por categoría */
    @GetMapping("/categorias/{categoriaId}/alimentos")
    public ResponseEntity<ApiResponse<PagedResponse<AlimentoResponse>>> getByCategoria(
            @PathVariable Integer categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Object principal) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(ApiResponse.ok("Alimentos de la categoría",
            catalogService.getAlimentosByCategoria(categoriaId, getUserId(principal), pageable)));
    }

    /** RF-27: Agregar a favoritos */
    @PostMapping("/favoritos/{alimentoId}")
    public ResponseEntity<ApiResponse<AlimentoResponse>> addFavorito(
            @PathVariable Integer alimentoId,
            @AuthenticationPrincipal Object principal) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok("Agregado a favoritos",
            catalogService.addFavorito(userId, alimentoId)));
    }

    /** RF-27: Eliminar de favoritos */
    @DeleteMapping("/favoritos/{alimentoId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorito(
            @PathVariable Integer alimentoId,
            @AuthenticationPrincipal Object principal) {
        catalogService.removeFavorito(getUserId(principal), alimentoId);
        return ResponseEntity.ok(ApiResponse.ok("Eliminado de favoritos", null));
    }

    /** RF-27: Ver mis favoritos */
    @GetMapping("/favoritos")
    public ResponseEntity<ApiResponse<List<AlimentoResponse>>> getFavoritos(
            @AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(ApiResponse.ok("Mis favoritos",
            catalogService.getFavoritos(getUserId(principal))));
    }
}
