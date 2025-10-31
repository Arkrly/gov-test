package com.ourvoiceourrights.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoIpService {

    @Value("${GEOIP2_DB_PATH:}")
    private String databasePath;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private DatabaseReader databaseReader;

    @PostConstruct
    void init() {
        if (StringUtils.isBlank(databasePath)) {
            log.info("GEOIP2_DB_PATH not configured; IP lookup disabled");
            return;
        }
        try {
            File dbFile = new File(databasePath);
            if (!dbFile.exists()) {
                log.warn("GeoIP database file not found at {}", databasePath);
                return;
            }
            lock.writeLock().lock();
            this.databaseReader = new DatabaseReader.Builder(dbFile).build();
            log.info("Loaded GeoIP database from {}", databasePath);
        } catch (IOException ex) {
            log.error("Failed to load GeoIP database", ex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<GeoLocation> locate(String ipAddress) {
        lock.readLock().lock();
        try {
            if (databaseReader == null) {
                return Optional.empty();
            }
            InetAddress address = InetAddress.getByName(ipAddress);
            CityResponse city = databaseReader.city(address);
            if (city == null) {
                return Optional.empty();
            }
            Double latitude = city.getLocation().getLatitude();
            Double longitude = city.getLocation().getLongitude();
            Integer accuracyRadius = city.getLocation().getAccuracyRadius();
            String state = city.getMostSpecificSubdivision().getName();
            String district = city.getCity().getName();
            return Optional.of(new GeoLocation(latitude, longitude, state, district, accuracyRadius == null ? null : accuracyRadius.doubleValue()));
        } catch (IOException | GeoIp2Exception ex) {
            log.warn("GeoIP lookup failed: {}", ex.getMessage());
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @PreDestroy
    void shutdown() {
        lock.writeLock().lock();
        try {
            if (databaseReader != null) {
                databaseReader.close();
            }
        } catch (IOException ex) {
            log.warn("Failed to close GeoIP database", ex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public record GeoLocation(Double latitude, Double longitude, String state, String district, Double accuracyKm) {
    }
}
