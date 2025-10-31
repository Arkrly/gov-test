package com.ourvoiceourrights.repository;

import com.ourvoiceourrights.entity.District;
import com.ourvoiceourrights.entity.MgnregaPerformance;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MgnregaPerformanceRepository extends JpaRepository<MgnregaPerformance, Long> {

    Optional<MgnregaPerformance> findTopByDistrictOrderByUpdatedAtDesc(District district);

    Optional<MgnregaPerformance> findByDistrictAndFinYearAndMonth(District district, String finYear, Integer month);

    Optional<MgnregaPerformance> findByDistrictAndSourceHash(District district, String sourceHash);

    Page<MgnregaPerformance> findByDistrictOrderByUpdatedAtDesc(District district, Pageable pageable);

    Page<MgnregaPerformance> findByDistrictAndFinYearOrderByUpdatedAtDesc(District district, String finYear, Pageable pageable);

    List<MgnregaPerformance> findByDistrictAndFinYearIn(District district, Collection<String> finYears);

    @Query("select max(p.updatedAt) from MgnregaPerformance p where p.updatedAt >= :threshold")
    Optional<Instant> findMostRecentUpdateSince(@Param("threshold") Instant threshold);
}
