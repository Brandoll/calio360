package com.calio.identity.repository;
import com.calio.identity.entity.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    @Query("SELECT g FROM UserGoal g WHERE g.user.id = :userId AND g.endDate IS NULL ORDER BY g.startDate DESC")
    Optional<UserGoal> findActiveGoalByUserId(Long userId);
}
