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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "districts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_district_state_name", columnNames = {"name", "state_id"}),
                @UniqueConstraint(name = "uk_district_code", columnNames = {"code", "state_id"})
        },
        indexes = {
                @Index(name = "idx_district_state", columnList = "state_id"),
                @Index(name = "idx_district_code", columnList = "code")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 24)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @Builder.Default
    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY)
    private Set<MgnregaPerformance> performances = new HashSet<>();
}
