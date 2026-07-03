package com.calio.foodcatalog.repository;
import com.calio.foodcatalog.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    Optional<Categoria> findByCodigoLetra(String codigoLetra);
}
