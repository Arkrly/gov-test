package com.ourvoiceourrights.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mgnrega_performance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_performance_unique", columnNames = {"district_id", "fin_year", "month"})
        },
        indexes = {
                @Index(name = "idx_performance_fin_year", columnList = "fin_year"),
                @Index(name = "idx_performance_updated", columnList = "updated_at"),
                @Index(name = "idx_performance_source_hash", columnList = "source_hash")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class MgnregaPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(name = "fin_year", nullable = false, length = 16)
    private String finYear;

    @Column
    private Integer month;

    @Column(name = "total_persondays")
    private Long totalPersondays;

    @Column(name = "total_households")
    private Long totalHouseholds;

    @Column(precision = 18, scale = 2)
    private BigDecimal expenditure;

    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.ingestedAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
