package com.devops;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void messageContainsHello() {
        assertTrue(App.message().contains("Hello"));
    }
}
