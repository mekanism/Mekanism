package mekanism.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test accessing various internal API definitions")
class APIAccessTest {

    @Test
    @DisplayName("Test getting the Radiation Manager")
    void testGetRadiationManager() {
        Assertions.assertNotNull(MekanismAPI.getRadiationManager());
    }
}