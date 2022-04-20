package mekanism.api;

import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.TooltipHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test accessing various internal API definitions")
class APIAccessTest {

    @Test
    @DisplayName("Test getting Module Helper")
    void testGetModuleHelper() {
        Assertions.assertInstanceOf(ModuleHelper.class, MekanismAPI.getModuleHelper());
    }

    @Test
    @DisplayName("Test getting the Radiation Manager")
    void testGetRadiationManager() {
        Assertions.assertInstanceOf(RadiationManager.class, MekanismAPI.getRadiationManager());
    }

    @Test
    @DisplayName("Test getting the Tooltip Helper")
    void testGetTooltipHelper() {
        Assertions.assertInstanceOf(TooltipHelper.class, MekanismAPI.getTooltipHelper());
    }

    @Test
    @DisplayName("Test getting Security Utils")
    void testGetSecurityUtils() {
        Assertions.assertInstanceOf(SecurityUtils.class, MekanismAPI.getSecurityUtils());
    }
}