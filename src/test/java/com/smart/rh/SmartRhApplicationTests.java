package com.smart.rh;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring application context loads cleanly.
 * Runs against the default H2 profile (no MySQL required).
 */
@SpringBootTest
@ActiveProfiles("test")
class SmartRhApplicationTests {

    @Test
    void contextLoads() {
        // Spring Boot will fail the test if the context does not start
    }
}
