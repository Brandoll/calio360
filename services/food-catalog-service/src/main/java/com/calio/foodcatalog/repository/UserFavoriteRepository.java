package com.calio.foodcatalog.repository;
import com.calio.foodcatalog.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    List<UserFavorite> findByUserId(Long userId);
    Optional<UserFavorite> findByUserIdAndAlimentoId(Long userId, Integer alimentoId);
    boolean existsByUserIdAndAlimentoId(Long userId, Integer alimentoId);
    void deleteByUserIdAndAlimentoId(Long userId, Integer alimentoId);
}
