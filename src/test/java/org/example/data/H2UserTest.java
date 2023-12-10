package org.example.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class H2UserTest {

    private H2User h2User;

    @BeforeEach
    public void setUp() {
        h2User = new H2User("Test Name", "test@example.com");
    }

    @Test
    public void testConstructor() {
        assertEquals("Test Name", h2User.getName());
        assertEquals("test@example.com", h2User.getEmail());
    }

    @Test
    public void testSetName() {
        h2User.setName("New Name");
        assertEquals("New Name", h2User.getName());
    }

    @Test
    public void testSetEmail() {
        h2User.setEmail("new@example.com");
        assertEquals("new@example.com", h2User.getEmail());
    }

    @Test
    public void testSetId() {
        h2User.setId(1L);
        assertEquals(1L, h2User.getId());
    }
}
