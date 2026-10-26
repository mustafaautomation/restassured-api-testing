package com.quvantic.api.retry;

import io.restassured.response.Response;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Retries a request when the server answers 429 (rate limited) or 503 (unavailable).
 *
 * <p>The wait honours a numeric {@code Retry-After} header (seconds), capped at {@code maxWait};
 * without one it backs off exponentially from {@code initialWait}. After {@code maxAttempts} the
 * last response is returned as is, so the test still sees and reports the failure status.
 */
public final class RateLimitRetry {

    private static final Set<Integer> RETRYABLE = Set.of(429, 503);

    /** Pluggable so unit tests can record waits instead of sleeping. */
    @FunctionalInterface
    public interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private final int maxAttempts;
    private final Duration initialWait;
    private final Duration maxWait;
    private final Sleeper sleeper;

    public RateLimitRetry(int maxAttempts, Duration initialWait, Duration maxWait, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialWait = Objects.requireNonNull(initialWait);
        this.maxWait = Objects.requireNonNull(maxWait);
        this.sleeper = Objects.requireNonNull(sleeper);
    }

    public static RateLimitRetry defaults() {
        return new RateLimitRetry(3, Duration.ofMillis(200), Duration.ofSeconds(5),
                d -> Thread.sleep(d.toMillis()));
    }

    public Response call(Supplier<Response> request) {
        Response response = request.get();
        for (int attempt = 1; attempt < maxAttempts && RETRYABLE.contains(response.statusCode()); attempt++) {
            Duration wait = waitFor(response.header("Retry-After"), attempt);
            try {
                sleeper.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return response;
            }
            response = request.get();
        }
        return response;
    }

    Duration waitFor(String retryAfter, int attempt) {
        Duration wait = initialWait.multipliedBy(1L << Math.min(attempt - 1, 20));
        if (retryAfter != null) {
            try {
                wait = Duration.ofSeconds(Math.max(0, Long.parseLong(retryAfter.trim())));
            } catch (NumberFormatException ignored) {
                // HTTP-date form is not supported; fall back to exponential backoff
            }
        }
        return wait.compareTo(maxWait) > 0 ? maxWait : wait;
    }
}
