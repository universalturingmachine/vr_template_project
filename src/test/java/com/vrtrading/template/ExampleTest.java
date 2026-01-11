package com.vrtrading.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Example Class Tests")
class ExampleTest {

    private Example example;

    @BeforeEach
    void setUp() {
        example = new Example();
    }

    @Test
    @DisplayName("getGreeting should return correct greeting message")
    void testGetGreeting() {
        // When
        String greeting = example.getGreeting();

        // Then
        assertNotNull(greeting, "Greeting should not be null");
        assertEquals("Hello from vr_template_project!", greeting, 
                "Greeting should match expected message");
    }

    @Test
    @DisplayName("getGreeting should return non-empty string")
    void testGetGreetingNotEmpty() {
        // When
        String greeting = example.getGreeting();

        // Then
        assertFalse(greeting.isEmpty(), "Greeting should not be empty");
        assertFalse(greeting.isEmpty(), "Greeting should have positive length");
    }

    @Test
    @DisplayName("getGreeting should return consistent result")
    void testGetGreetingConsistency() {
        // When
        String greeting1 = example.getGreeting();
        String greeting2 = example.getGreeting();

        // Then
        assertEquals(greeting1, greeting2, 
                "Multiple calls to getGreeting should return the same value");
    }

    @Test
    @DisplayName("Multiple Example instances should return same greeting")
    void testMultipleInstancesReturnSameGreeting() {
        // Given
        Example example1 = new Example();
        Example example2 = new Example();

        // When
        String greeting1 = example1.getGreeting();
        String greeting2 = example2.getGreeting();

        // Then
        assertEquals(greeting1, greeting2, 
                "Different instances should return the same greeting");
    }
}

