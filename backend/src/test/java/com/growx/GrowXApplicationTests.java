package com.growx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring application context loads successfully.
 * Uses the "test" profile to allow context loading without a real DB connection.
 */
@SpringBootTest
@ActiveProfiles("test")
class GrowXApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context starts without throwing, the test passes.
        // This catches configuration errors, missing beans, and circular dependencies.
    }
}
