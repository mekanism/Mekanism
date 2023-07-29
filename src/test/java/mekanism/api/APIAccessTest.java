package mekanism.api;

import mekanism.api.gear.IModuleHelper;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.ITooltipHelper;
import mekanism.common.service.MekanismAccess;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.lib.radial.data.RadialDataHelper;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.TooltipHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test accessing various internal API definitions")
class APIAccessTest {

    @Test
    @DisplayName("Test getting access to Mekanism internals")
    void testMekanismAccess() {
        Assertions.assertInstanceOf(MekanismAccess.class, IMekanismAccess.INSTANCE);
    }

    @Test
    @DisplayName("Test getting Module Helper")
    void testGetModuleHelper() {
        Assertions.assertInstanceOf(ModuleHelper.class, IModuleHelper.INSTANCE);
    }

    @Test
    @DisplayName("Test getting the RadialData Helper")
    void testGetRadialDataHelper() {
        Assertions.assertInstanceOf(RadialDataHelper.class, IRadialDataHelper.INSTANCE);
    }

    @Test
    @DisplayName("Test getting the Radiation Manager")
    void testGetRadiationManager() {
        Assertions.assertInstanceOf(RadiationManager.class, IRadiationManager.INSTANCE);
    }

    @Test
    @DisplayName("Test getting Security Utils")
    void testGetSecurityUtils() {
        Assertions.assertInstanceOf(SecurityUtils.class, ISecurityUtils.INSTANCE);
    }

    @Test
    @DisplayName("Test getting the Tooltip Helper")
    void testGetTooltipHelper() {
        Assertions.assertInstanceOf(TooltipHelper.class, ITooltipHelper.INSTANCE);
    }
}