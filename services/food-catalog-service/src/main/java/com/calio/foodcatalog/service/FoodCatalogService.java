package com.calio.foodcatalog.service;
import com.calio.foodcatalog.dto.response.*;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface FoodCatalogService {
    List<CategoriaResponse> getAllCategorias();
    AlimentoResponse getAlimentoById(Integer id, Long userId);
    AlimentoResponse getAlimentoByCodigo(String codigo, Long userId);
    PagedResponse<AlimentoResponse> searchAlimentos(String query, Integer categoriaId, Long userId, Pageable pageable);
    PagedResponse<AlimentoResponse> getAlimentosByCategoria(Integer categoriaId, Long userId, Pageable pageable);
    AlimentoResponse addFavorito(Long userId, Integer alimentoId);
    void removeFavorito(Long userId, Integer alimentoId);
    List<AlimentoResponse> getFavoritos(Long userId);
}
