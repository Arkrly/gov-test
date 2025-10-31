package com.ourvoiceourrights.repository;

import com.ourvoiceourrights.entity.District;
import com.ourvoiceourrights.entity.State;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, Long> {

    List<District> findByStateOrderByNameAsc(State state);

    Optional<District> findByStateAndNameIgnoreCase(State state, String name);

    Optional<District> findByStateCodeIgnoreCaseAndNameIgnoreCase(String stateCode, String name);

    Optional<District> findByCodeIgnoreCaseAndStateCodeIgnoreCase(String code, String stateCode);

    Optional<District> findByNameIgnoreCase(String name);

    Optional<District> findByCodeIgnoreCase(String code);
}
