package com.calio.foodcatalog.repository;
import com.calio.foodcatalog.entity.Alimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface AlimentoRepository extends JpaRepository<Alimento, Integer> {
    Optional<Alimento> findByCodigoIns(String codigoIns);
    Page<Alimento> findByCategoriaId(Integer categoriaId, Pageable pageable);
    @Query("SELECT a FROM Alimento a WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Alimento> searchByNombre(@Param("q") String q, Pageable pageable);
    @Query("SELECT a FROM Alimento a WHERE a.categoria.id = :catId AND LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Alimento> searchByNombreAndCategoria(@Param("q") String q, @Param("catId") Integer catId, Pageable pageable);
}
