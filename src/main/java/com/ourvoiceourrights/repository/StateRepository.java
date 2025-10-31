package com.ourvoiceourrights.repository;

import com.ourvoiceourrights.entity.State;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Long> {

    Optional<State> findByCodeIgnoreCase(String code);

    Optional<State> findByNameIgnoreCase(String name);
}
