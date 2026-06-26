package mekanism.api.chemical;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import mekanism.common.registries.MekanismChemicals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test that the slurry ids we expose in the API match what we generate internally")
class ChemicalsDefinedTest {

    @Test
    @DisplayName("Test that we expose a slurry id in the API for every slurry we generate")
    void testSlurryIds() throws IllegalAccessException {
        Set<CleanDirtySlurryId> slurryIds = new HashSet<>();
        for (Field field : ChemicalIds.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().endsWith("SLURRY")) {
                slurryIds.add((CleanDirtySlurryId) field.get(null));
            }
        }

        for (CleanDirtySlurryId value : MekanismChemicals.PROCESSED_RESOURCES.values()) {
            Assertions.assertTrue(slurryIds.contains(value), "API does not expose " + value);
        }
    }
}