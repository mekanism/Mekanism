package mekanism.api.energy;

import mekanism.common.service.EnergyConversionHelper;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test accessing energy conversion helpers")
class EnergyConversionHelperAccessTest {

    @Test
    @DisplayName("Test getting access to the energy conversion helper")
    void testEnergyConversionHelper() {
        Assertions.assertInstanceOf(EnergyConversionHelper.class, IEnergyConversionHelper.INSTANCE);
    }

    @Test
    @DisplayName("Test that all conversions have a getter and a validation test")
    void testConversions() {
        int energyUnitCount = EnergyUnit.values().length;
        //Validate everything references the correct one
        int types = assertEquals(EnergyUnit.JOULES, IEnergyConversionHelper.INSTANCE.jouleConversion());
        types += assertEquals(EnergyUnit.FORGE_ENERGY, IEnergyConversionHelper.INSTANCE.feConversion());
        //And finally validate that we actually have a test for every type
        Assertions.assertEquals(energyUnitCount, types);
    }

    private static int assertEquals(Object expected, Object actual) {
        Assertions.assertEquals(expected, actual);
        return 1;
    }
}