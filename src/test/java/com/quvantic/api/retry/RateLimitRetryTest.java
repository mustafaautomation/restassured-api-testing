package com.quvantic.api.retry;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitRetryTest {

    private final RateLimitRetry retry =
            new RateLimitRetry(4, Duration.ofMillis(100), Duration.ofSeconds(3), d -> {});

    @Test
    void backsOffExponentiallyWithoutRetryAfter() {
        assertThat(retry.waitFor(null, 1)).isEqualTo(Duration.ofMillis(100));
        assertThat(retry.waitFor(null, 2)).isEqualTo(Duration.ofMillis(200));
        assertThat(retry.waitFor(null, 3)).isEqualTo(Duration.ofMillis(400));
    }

    @Test
    void usesRetryAfterSeconds() {
        assertThat(retry.waitFor("2", 1)).isEqualTo(Duration.ofSeconds(2));
        assertThat(retry.waitFor(" 0 ", 3)).isEqualTo(Duration.ZERO);
    }

    @Test
    void capsTheWait() {
        assertThat(retry.waitFor("120", 1)).isEqualTo(Duration.ofSeconds(3));
        assertThat(retry.waitFor(null, 30)).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void ignoresHttpDateRetryAfter() {
        assertThat(retry.waitFor("Wed, 21 Oct 2026 07:28:00 GMT", 2)).isEqualTo(Duration.ofMillis(200));
    }

    @Test
    void rejectsZeroAttempts() {
        assertThatThrownBy(() -> new RateLimitRetry(0, Duration.ZERO, Duration.ZERO, d -> {}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
