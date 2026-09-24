package com.mechanicmanager.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cuenta eventos por clave dentro de una ventana deslizante, en memoria.
 *
 * <p>Alcanza para una sola instancia del backend: el estado se pierde al
 * reiniciar y no se comparte entre instancias. Si el despliegue pasa a tener
 * varias, hay que moverlo a un almacén compartido.
 *
 * <p>No decide qué es un intento ni cuántos alcanzan: quien lo usa registra
 * eventos y consulta con su propio máximo.
 */
@Component
public class AttemptLimiter {

    /** Tope de claves guardadas: evita que claves inventadas hagan crecer la memoria sin límite. */
    static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration window;

    public AttemptLimiter(
            Clock clock,
            @Value("${security.rate-limit.window-minutes:15}") long windowMinutes
    ) {
        this.clock = clock;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    /** {@code true} si la clave ya acumuló {@code max} eventos dentro de la ventana. */
    public boolean isBlocked(String key, int max) {
        Deque<Instant> events = attempts.get(key);

        if (events == null) {
            return false;
        }

        synchronized (events) {
            discardExpired(events);
            return events.size() >= max;
        }
    }

    public void record(String key) {
        if (attempts.size() >= MAX_TRACKED_KEYS) {
            purgeExpired();
        }

        Deque<Instant> events = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (events) {
            discardExpired(events);
            events.addLast(clock.instant());
        }
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private void discardExpired(Deque<Instant> events) {
        Instant limit = clock.instant().minus(window);

        while (!events.isEmpty() && events.peekFirst().isBefore(limit)) {
            events.pollFirst();
        }
    }

    private void purgeExpired() {
        attempts.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                discardExpired(entry.getValue());
                return entry.getValue().isEmpty();
            }
        });
    }
}
