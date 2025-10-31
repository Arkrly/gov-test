package com.ourvoiceourrights.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

@Entity
@Table(name = "district_boundaries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_boundary_district_state", columnNames = {"district_name", "state_name"})
        },
        indexes = {
                @Index(name = "idx_boundary_state", columnList = "state_name"),
                @Index(name = "idx_boundary_district", columnList = "district_name")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class DistrictBoundary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "district_name", nullable = false, length = 160)
    private String districtName;

    @Column(name = "state_name", nullable = false, length = 120)
    private String stateName;

    @Column(columnDefinition = "geometry")
    private Polygon geometry;
}
