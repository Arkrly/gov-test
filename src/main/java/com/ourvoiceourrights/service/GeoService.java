package com.ourvoiceourrights.service;

import com.ourvoiceourrights.client.ReverseGeocodeClient;
import com.ourvoiceourrights.dto.GeoDetectDto;
import com.ourvoiceourrights.dto.GeoReverseDto;
import com.ourvoiceourrights.entity.DistrictBoundary;
import com.ourvoiceourrights.repository.DistrictBoundaryRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class GeoService {

    private final GeoIpService geoIpService;
    private final ReverseGeocodeClient reverseGeocodeClient;
    private final DistrictBoundaryRepository boundaryRepository;
    private final GeometryFactory geometryFactory;

    public GeoService(GeoIpService geoIpService,
                      ReverseGeocodeClient reverseGeocodeClient,
                      DistrictBoundaryRepository boundaryRepository) {
        this.geoIpService = geoIpService;
        this.reverseGeocodeClient = reverseGeocodeClient;
        this.boundaryRepository = boundaryRepository;
        this.geometryFactory = new GeometryFactory();
    }

    public Optional<GeoDetectDto> detect(Double latitude, Double longitude, String clientIp) {
    if (latitude != null && longitude != null) {
        return resolveByCoordinates(latitude, longitude)
            .map(result -> new GeoDetectDto(result.state(), result.district(), result.method(), result.accuracyKm()));
        }

        if (StringUtils.isNotBlank(clientIp)) {
            return geoIpService.locate(clientIp)
                    .map(location -> new GeoDetectDto(
                            location.state(),
                            location.district(),
                            "geoip",
                            location.accuracyKm()));
        }
        return Optional.empty();
    }

    public Optional<GeoReverseDto> reverse(Double latitude, Double longitude) {
        return reverseGeocodeClient.reverse(latitude, longitude)
                .map(result -> new GeoReverseDto(
                        result.address(),
                        result.state(),
                        result.district(),
                        "reverse-geocode",
                        result.accuracyKm()));
    }

    private Optional<GeoDetectionResult> resolveByCoordinates(Double latitude, Double longitude) {
    return reverseGeocodeClient.reverse(latitude, longitude)
        .map(result -> new GeoDetectionResult(result.state(), result.district(), result.accuracyKm(), "reverse-geocode"))
                .or(() -> lookupOffline(latitude, longitude));
    }

    private Optional<GeoDetectionResult> lookupOffline(Double latitude, Double longitude) {
        try {
            Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            return boundaryRepository.findContaining(point)
            .map(boundary -> new GeoDetectionResult(
                boundary.getStateName(),
                boundary.getDistrictName(),
                25.0,
                "offline-spatial"));
        } catch (Exception ex) {
            log.warn("Offline geo lookup failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private record GeoDetectionResult(String state, String district, Double accuracyKm, String method) {
    }
}
