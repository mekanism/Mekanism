package mekanism.api.energy;

import mekanism.api.energy.EnergyConversionHelper.EnergyConversions;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test accessing energy conversion helpers")
class EnergyConversionHelperAccessTest {

    @Test
    @DisplayName("Test looking up energy conversions")
    void testGetEnergyUnits() {
        Assertions.assertArrayEquals(EnergyUnit.values(), EnergyConversionHelper.getEnergyConversions());
    }

    @Test
    @DisplayName("Test that all conversions have a getter and a validation test")
    void testConversions() {
        int energyUnitCount = EnergyUnit.values().length;
        //Validate we have the same number of conversion holders
        Assertions.assertEquals(energyUnitCount, EnergyConversions.values().length);
        //Validate everything references the correct one
        int types = assertEquals(EnergyUnit.JOULES, EnergyConversionHelper.jouleConversion());
        types += assertEquals(EnergyUnit.FORGE_ENERGY, EnergyConversionHelper.feConversion());
        types += assertEquals(EnergyUnit.ELECTRICAL_UNITS, EnergyConversionHelper.euConversion());
        //And finally validate that we actually have a test for every type
        Assertions.assertEquals(energyUnitCount, types);
    }

    private static int assertEquals(Object expected, Object actual) {
        Assertions.assertEquals(expected, actual);
        return 1;
    }
}