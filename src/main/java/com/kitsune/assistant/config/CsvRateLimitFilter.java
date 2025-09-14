package com.kitsune.assistant.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CsvRateLimitFilter extends OncePerRequestFilter {

  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if ("/admin/import/csv".equals(request.getRequestURI())) {
      String key = request.getRemoteAddr();
      Bucket bucket = cache.computeIfAbsent(key, this::newBucket);
      if (!bucket.tryConsume(1)) {
        response.setStatus(429);
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  private Bucket newBucket(String key) {
    return Bucket.builder()
        .addLimit(Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1))))
        .build();
  }
}
