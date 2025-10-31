package com.ourvoiceourrights.service;

import com.ourvoiceourrights.config.AppProperties;
import com.ourvoiceourrights.dto.StateDto;
import com.ourvoiceourrights.entity.State;
import com.ourvoiceourrights.repository.StateRepository;
import com.ourvoiceourrights.service.cache.CacheNames;
import com.ourvoiceourrights.service.cache.CacheService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StateService {

    private final StateRepository stateRepository;
    private final CacheService cacheService;
    private final AppProperties appProperties;

    public List<StateDto> listStates() {
        return cacheService.getList(CacheNames.STATES, "__ALL__", StateDto.class)
                .orElseGet(() -> {
                    List<StateDto> states = stateRepository.findAll().stream()
                            .sorted(Comparator.comparing(State::getName))
                            .map(state -> new StateDto(state.getId(), state.getName(), state.getCode()))
                            .collect(Collectors.toList());
                    cacheService.put(CacheNames.STATES, "__ALL__", states, appProperties.getCache().getStatesTtl());
                    return states;
                });
    }
}
