package com.snakeleaderboard.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple, database-backed rate limiter for score submissions.
 *
 * <p>Applies a fixed request limit to {@code POST /api/scores} per client IP within a rolling
 * one-minute window. Buckets are stored in the {@code rate_limits} table so that rate limiting
 * works consistently across multiple application instances.</p>
 *
 * <h2>Forwarded IP handling</h2>
 * <p>When {@code rate_limit.trust_forwarded_headers=true}, the filter will use the first value in
 * {@code X-Forwarded-For} <em>only</em> if {@link HttpServletRequest#getRemoteAddr()} matches a
 * trusted proxy. Trusted proxies are configured via {@code rate_limit.trusted_proxies} as a
 * comma-separated list of IPv4 addresses or CIDR ranges (e.g. {@code 10.0.0.0/8}), or {@code *}
 * to trust all proxies.</p>
 */
@Component
public class RateLimitFilter implements Filter {

    private static final int LIMIT = 30; // requests per window
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final JdbcClient jdbc;
    private final boolean trustForwardedHeaders;
    private final boolean trustAllProxies;
    private final List<IpRange> trustedProxies;

    public RateLimitFilter(
            JdbcClient jdbc,
            @Value("${rate_limit.trust_forwarded_headers:false}") boolean trustForwardedHeaders,
            @Value("${rate_limit.trusted_proxies:}") String trustedProxiesConfig
    ) {
        this.jdbc = jdbc;
        this.trustForwardedHeaders = trustForwardedHeaders;
        String trimmed = trustedProxiesConfig == null ? "" : trustedProxiesConfig.trim();
        if ("*".equals(trimmed)) {
            this.trustAllProxies = true;
            this.trustedProxies = List.of();
        } else {
            this.trustAllProxies = false;
            this.trustedProxies = IpRange.parseList(trimmed);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        // Only rate limit score submissions (recommended)
        if (!("POST".equalsIgnoreCase(req.getMethod())
                && ("/api/scores".equals(path) || "/api/scores/".equals(path)))) {
            chain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(req);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (isAllowed(ip, now)) {
            chain.doFilter(request, response);
            return;
        }

        res.setStatus(429);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"Too Many Requests\"}");
    }

    /**
     * Resolves the client IP address for rate limiting.
     *
     * <p>By default, this uses {@link HttpServletRequest#getRemoteAddr()}. If forwarded headers are
     * enabled and the direct peer is a trusted proxy, it uses the first address in
     * {@code X-Forwarded-For}.</p>
     */
    private String resolveClientIp(HttpServletRequest req) {
        String remoteAddr = req.getRemoteAddr();
        if (!trustForwardedHeaders || !isTrustedProxy(remoteAddr)) {
            return remoteAddr;
        }
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            return remoteAddr;
        }
        return forwarded.split(",")[0].trim();
    }

    /**
     * Checks whether {@code remoteAddr} is allowed to supply forwarded headers.
     */
    private boolean isTrustedProxy(String remoteAddr) {
        if (trustAllProxies) {
            return true;
        }
        if (trustedProxies.isEmpty()) {
            return false;
        }
        for (IpRange range : trustedProxies) {
            if (range.matches(remoteAddr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Records this request in the current window and determines whether it is allowed.
     *
     * <p>The implementation uses a single upsert to either (a) start a new window for the IP when
     * the previous window has expired or (b) increment the current window's request count.</p>
     *
     * @param ip client IP bucket key
     * @param now current timestamp in UTC
     * @return {@code true} when under the limit; {@code false} when the limit has been exceeded
     */
    private boolean isAllowed(String ip, OffsetDateTime now) {
        OffsetDateTime windowStart = now.minus(WINDOW);

        Integer count = jdbc.sql("""
                WITH upsert AS (
                    INSERT INTO rate_limits (ip, window_start, count)
                    VALUES (?, ?, 1)
                    ON CONFLICT (ip) DO UPDATE
                    SET window_start = CASE
                            WHEN rate_limits.window_start <= ? THEN EXCLUDED.window_start
                            ELSE rate_limits.window_start
                        END,
                        count = CASE
                            WHEN rate_limits.window_start <= ? THEN 1
                            ELSE rate_limits.count + 1
                        END
                    RETURNING count
                )
                SELECT count FROM upsert
                """)
                .params(ip, now, windowStart, windowStart)
                .query(Integer.class)
                .single();

        return count != null && count <= LIMIT;
    }

    private static final class IpRange {
        private final String exact;
        private final Integer base;
        private final Integer mask;

        private IpRange(String exact, Integer base, Integer mask) {
            this.exact = exact;
            this.base = base;
            this.mask = mask;
        }

        /**
         * Parses a comma-separated list of proxy ranges.
         *
         * <p>Entries may be exact addresses (string match) or IPv4 CIDR ranges (e.g. {@code 10.0.0.0/8}).</p>
         */
        static List<IpRange> parseList(String config) {
            if (config == null || config.isBlank()) {
                return List.of();
            }
            String[] values = config.split(",");
            List<IpRange> ranges = new ArrayList<>();
            for (String value : values) {
                String trimmed = value.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                ranges.add(parse(trimmed));
            }
            return List.copyOf(ranges);
        }

        /**
         * Parses a single proxy range expression.
         *
         * <p>CIDR ranges are IPv4-only; exact matches are compared as raw strings (so IPv6 literals
         * can be used as exact matches).</p>
         *
         * @throws IllegalArgumentException if the value is not a valid IPv4/CIDR expression
         */
        static IpRange parse(String value) {
            if (value.contains("/")) {
                String[] parts = value.split("/", 2);
                int baseIp = ipv4ToInt(parts[0]);
                int prefix = Integer.parseInt(parts[1]);
                if (prefix < 0 || prefix > 32) {
                    throw new IllegalArgumentException("Invalid CIDR prefix: " + value);
                }
                int mask = prefix == 0 ? 0 : (-1 << (32 - prefix));
                int base = baseIp & mask;
                return new IpRange(null, base, mask);
            }
            return new IpRange(value, null, null);
        }

        /**
         * Returns whether the given IP belongs to this range.
         */
        boolean matches(String ip) {
            if (exact != null) {
                return exact.equals(ip);
            }
            try {
                int ipVal = ipv4ToInt(ip);
                return (ipVal & mask) == base;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

        /**
         * Parses an IPv4 address into an integer.
         *
         * @throws IllegalArgumentException if the value is not a valid IPv4 literal
         */
        private static int ipv4ToInt(String ip) {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
            }
            int result = 0;
            for (String part : parts) {
                int val = Integer.parseInt(part);
                if (val < 0 || val > 255) {
                    throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
                }
                result = (result << 8) | val;
            }
            return result;
        }
    }
}
