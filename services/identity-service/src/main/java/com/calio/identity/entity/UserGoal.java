package com.calio.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Table(name = "user_goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserGoal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type")
    private GoalType goalType;
    @Column(name = "target_weight_kg", precision = 5, scale = 2)
    private BigDecimal targetWeightKg;
    @Column(name = "daily_calories")
    private Integer dailyCalories;
    @Column(name = "protein_grams", precision = 5, scale = 2)
    private BigDecimal proteinGrams;
    @Column(name = "carbs_grams", precision = 5, scale = 2)
    private BigDecimal carbsGrams;
    @Column(name = "fat_grams", precision = 5, scale = 2)
    private BigDecimal fatGrams;
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level")
    private ActivityLevel activityLevel;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @PrePersist void prePersist() {
        this.createdAt = OffsetDateTime.now();
        if (this.startDate == null) this.startDate = LocalDate.now();
    }
    public enum GoalType { LOSE_WEIGHT, GAIN_MUSCLE, MAINTAIN, EAT_HEALTHY }
    public enum ActivityLevel { SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE }
}
