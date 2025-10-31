package com.ourvoiceourrights.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourvoiceourrights.dto.ErrorDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final Map<String, RequestCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String ip = resolveClientIp(request);
        int limit = appProperties.getRateLimit().getRequestsPerMinute();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);

        RequestCounter counter = counters.compute(ip, (key, existing) -> {
            if (existing == null || !existing.windowStart.equals(now)) {
                return new RequestCounter(now, 1);
            }
            existing.increment();
            return existing;
        });

        if (counter.count > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorDto error = new ErrorDto(
                    Instant.now(),
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                    "Rate limit exceeded",
                    request.getRequestURI(),
                    request.getHeader("X-B3-TraceId")
            );
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class RequestCounter {
        private final Instant windowStart;
        private int count;

        private RequestCounter(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }

        private void increment() {
            this.count++;
        }
    }
}
