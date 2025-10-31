package com.ourvoiceourrights.repository;

import com.ourvoiceourrights.entity.DistrictBoundary;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DistrictBoundaryRepository extends JpaRepository<DistrictBoundary, Long> {

    Optional<DistrictBoundary> findByDistrictNameIgnoreCaseAndStateNameIgnoreCase(String districtName, String stateName);

    @Query("select b from DistrictBoundary b where ST_Contains(b.geometry, :point) = true")
    Optional<DistrictBoundary> findContaining(@Param("point") Point point);
}
