package com.example.splitbill.service;

import com.example.splitbill.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * จำกัดจำนวนครั้งที่ "ทำไม่สำเร็จ" ต่อผู้ใช้ต่อการกระทำ เช่น กรอกรหัสบิลผิด หรือค้นอีเมลที่ไม่มีอยู่
 * กันการสุ่มเดารหัสเข้าร่วมบิล / ไล่หาอีเมลผู้ใช้ (เก็บในหน่วยความจำ — พอสำหรับเครื่องเดียว)
 */
@Component
public class AttemptLimiter {
    static final Duration WINDOW = Duration.ofMinutes(10);

    private final Map<String, Window> failures = new ConcurrentHashMap<>();
    private final Clock clock;

    public AttemptLimiter() {
        this(Clock.systemUTC());
    }

    AttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    /** เรียกก่อนลองทำ — ถ้าภายใน 10 นาทีผิดครบ maxFailures ครั้งแล้วจะโยน 429 */
    public void check(String action, Long userId, int maxFailures) {
        Window w = failures.get(key(action, userId));
        if (w != null && !w.expired(clock.instant()) && w.count >= maxFailures) {
            throw new TooManyRequestsException("ลองผิดหลายครั้งเกินไป กรุณารอสักครู่แล้วลองใหม่");
        }
    }

    /** เรียกเมื่อทำไม่สำเร็จ */
    public void recordFailure(String action, Long userId) {
        Instant now = clock.instant();
        failures.compute(key(action, userId), (k, w) ->
                (w == null || w.expired(now)) ? new Window(now, 1) : new Window(w.start, w.count + 1));
    }

    /** เรียกเมื่อทำสำเร็จ — ล้างตัวนับ */
    public void reset(String action, Long userId) {
        failures.remove(key(action, userId));
    }

    private static String key(String action, Long userId) {
        return action + ":" + userId;
    }

    private record Window(Instant start, int count) {
        boolean expired(Instant now) {
            return now.isAfter(start.plus(WINDOW));
        }
    }
}
