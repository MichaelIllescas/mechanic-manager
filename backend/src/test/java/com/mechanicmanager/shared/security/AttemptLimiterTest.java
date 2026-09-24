package com.mechanicmanager.shared.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AttemptLimiterTest {

    private final MutableClock clock = new MutableClock();
    private final AttemptLimiter limiter = new AttemptLimiter(clock, 15);

    @Test
    void doesNotBlockBeforeReachingTheMaximum() {
        limiter.record("k");
        limiter.record("k");

        assertThat(limiter.isBlocked("k", 3)).isFalse();
    }

    @Test
    void blocksOnceTheMaximumIsReached() {
        limiter.record("k");
        limiter.record("k");
        limiter.record("k");

        assertThat(limiter.isBlocked("k", 3)).isTrue();
    }

    @Test
    void keepsKeysIndependent() {
        limiter.record("a");
        limiter.record("a");

        assertThat(limiter.isBlocked("b", 2)).isFalse();
    }

    @Test
    void forgetsEventsOutsideTheWindow() {
        limiter.record("k");
        limiter.record("k");

        clock.advance(Duration.ofMinutes(16));

        assertThat(limiter.isBlocked("k", 2)).isFalse();
    }

    @Test
    void resetClearsTheKey() {
        limiter.record("k");
        limiter.record("k");

        limiter.reset("k");

        assertThat(limiter.isBlocked("k", 2)).isFalse();
    }

    @Test
    void neverBlocksAnUnknownKey() {
        assertThat(limiter.isBlocked("nunca-visto", 1)).isFalse();
    }

    private static final class MutableClock extends Clock {

        private Instant now = Instant.parse("2026-09-24T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
