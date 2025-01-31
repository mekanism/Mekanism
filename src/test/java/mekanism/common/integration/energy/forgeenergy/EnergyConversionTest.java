package mekanism.common.integration.energy.forgeenergy;

import java.util.Collections;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Forge Energy conversion")
class EnergyConversionTest {

    private final double CONVERSION_RATE = 2.5;
    private final double INVERSE_CONVERSION = 1 / CONVERSION_RATE;
    private final int JOULES_CAPACITY = 1_000;
    private final int FE_CAPACITY = 400; // capacity / CONVERSION_RATE

    // WRAPPING STRICT ENERGY TO FORGE ENERGY

    @Test
    @DisplayName("Test wrapping J to FE against a small empty container")
    void testJoulesAsFESmallEmptyToFull() {
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check nothing can be extracted
        int extractedSimulate = feHandler.extractEnergy(JOULES_CAPACITY, true);
        int extracted = feHandler.extractEnergy(JOULES_CAPACITY, false);
        Assertions.assertEquals(extractedSimulate, extracted, "Simulation should be the same");
        assertValueEqual(extracted, 0, "extracted energy (fe)");
        assertValueEqual(feHandler.getMaxEnergyStored(), FE_CAPACITY, "FE capacity");

        //insert more than the FE capacity, check it capped at FE max
        int acceptedSimulate = feHandler.receiveEnergy(JOULES_CAPACITY, true);
        int accepted = feHandler.receiveEnergy(JOULES_CAPACITY, false);
        Assertions.assertEquals(acceptedSimulate, accepted, "Simulation should be the same");
        assertValueEqual(accepted, FE_CAPACITY, "Accepted FE");
        assertValueEqual(feHandler.getEnergyStored(), FE_CAPACITY, "stored energy (fe)");
        assertValueEqual(joulesContainer.getEnergy(), (long) JOULES_CAPACITY, "stored energy (joules)");
    }

    @Test
    @DisplayName("Test wrapping J to FE against a small empty container, rejecting sub-unit values")
    void testJoulesAsFESmallEmptySubUnit() {
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);

        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);
        int acceptedSimulate = feHandler.receiveEnergy(1, true);
        int accepted = feHandler.receiveEnergy(1, false);
        assertValueEqual(acceptedSimulate, 0, "accepted energy");
        assertValueEqual(accepted, 0, "accepted energy");
        assertValueEqual(feHandler.getEnergyStored(), 0, "stored energy");
        assertValueEqual(joulesContainer.getEnergy(), 0L, "raw stored energy");
    }

    @Test
    @DisplayName("Test wrapping J to FE against a small full container")
    void testJoulesAsFESmallFull() {
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        joulesContainer.setEnergy(JOULES_CAPACITY);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //try to insert to full container
        int accepted = feHandler.receiveEnergy(JOULES_CAPACITY, true);
        assertValueEqual(accepted, 0, "accepted energy when full");
        accepted = feHandler.receiveEnergy(JOULES_CAPACITY, false);
        assertValueEqual(accepted, 0, "accepted energy when full");

        //extract beyond converted capacity
        int extractedFE = feHandler.extractEnergy(JOULES_CAPACITY, true);
        assertValueEqual(extractedFE, FE_CAPACITY, "extracted energy (fe)");
        extractedFE = feHandler.extractEnergy(JOULES_CAPACITY, false);
        assertValueEqual(extractedFE, FE_CAPACITY, "extracted energy (fe)");
        assertValueEqual(feHandler.getEnergyStored(), 0, "stored energy (fe)");
        assertValueEqual(joulesContainer.getEnergy(), 0L, "stored energy (joules)");
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (which cant fit full conversion)")
    void testJoulesAsFENearlyFullNoAccept() {
        //Note: There shouldn't be any room for it
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        joulesContainer.setEnergy(JOULES_CAPACITY - 2);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check
        //Note: Needed energy should be 1 even though we can't accept it

        assertValueEqual(feHandler.getEnergyStored(), FE_CAPACITY - 1, "stored energy");
        assertValueEqual(feHandler.getMaxEnergyStored(), FE_CAPACITY, "max energy");

        int accepted = feHandler.receiveEnergy(JOULES_CAPACITY, true);
        assertValueEqual(accepted, 0, "accepted energy (fe)");
        accepted = feHandler.receiveEnergy(JOULES_CAPACITY, false);
        assertValueEqual(accepted, 0, "accepted energy (fe)");
        assertValueEqual(joulesContainer.getEnergy(), (long) JOULES_CAPACITY - 2, "joules contents");
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (with enough for just over the conversion rate)")
    void testJoulesAsFEFullContainerPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
        joulesContainer.setEnergy(997);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check.
        assertValueEqual(feHandler.getEnergyStored(), 398, "stored energy");

        int accepted = feHandler.receiveEnergy(1000, true);
        assertValueEqual(accepted, 0, "accepted energy (fe)");
        accepted = feHandler.receiveEnergy(1000, false);
        assertValueEqual(joulesContainer.getEnergy(), 997L, "stored joules after insert");
        assertValueEqual(accepted, 0, "accepted energy (fe)");
    }

    @Test
    @DisplayName("Test wrapping J to FE extracting against a small nearly empty container (less than conversion rate)")
    void testJoulesAsFEExtractNotEnough() {
        //Note: There shouldn't be enough to get a single unit out
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        joulesContainer.setEnergy(2);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        int extracted = feHandler.extractEnergy(JOULES_CAPACITY, true);
        assertValueEqual(extracted, 0, "extracted energy (fe)");
        extracted = feHandler.extractEnergy(JOULES_CAPACITY, false);
        assertValueEqual(extracted, 0, "extracted energy (fe)");
        assertValueEqual(joulesContainer.getEnergy(), 2L, "stored energy (joules)");
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (with enough for two full and one partial unit 8J)")
    void testJoulesAsFEInsertPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
        joulesContainer.setEnergy(992);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check.
        assertValueEqual(feHandler.getEnergyStored(), 396, "stored energy");

        int accepted = feHandler.receiveEnergy(1000, true);
        assertValueEqual(accepted, 2, "accepted energy (fe)");

        accepted = feHandler.receiveEnergy(1000, false);
        assertValueEqual(joulesContainer.getEnergy(), 997L, "stored joules after insert");
        assertValueEqual(accepted, 2, "accepted energy (fe)");
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small empty container with an uneven insert")
    void testJoulesAsFEEmptyContainerPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check.
        assertValueEqual(feHandler.getEnergyStored(), 0, "stored energy (fe)");

        int accepted = feHandler.receiveEnergy(3, true);
        assertValueEqual(accepted, 2, "accepted energy (fe)");

        accepted = feHandler.receiveEnergy(3, false);
        assertValueEqual(joulesContainer.getEnergy(), 5L, "stored joules after insert");
        assertValueEqual(accepted, 2, "accepted energy (fe)");
    }

    @Test
    @DisplayName("Test wrapping J to FE extracting against a small nearly empty container (2.5x conversion)")
    void testJoulesAsFEExtractPartial() {
        //Note: There should be enough to get 2 converted units out
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        joulesContainer.setEnergy(8);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        int extracted = feHandler.extractEnergy(JOULES_CAPACITY, true);
        assertValueEqual(extracted, 2, "extracted energy (fe)");

        extracted = feHandler.extractEnergy(JOULES_CAPACITY, false);
        assertValueEqual(extracted, 2, "extracted energy (fe)");
        assertValueEqual(joulesContainer.getEnergy(), 3L, "stored energy (joules)");
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a sub one sized container")
    void testJoulesAsFECantFit() {
        //Note: There shouldn't be any room for it
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(2, null);
        joulesContainer.setEnergy(0);
        IEnergyStorage feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        int accepted = feHandler.receiveEnergy(JOULES_CAPACITY, true);
        assertValueEqual(accepted, 0, "accepted energy (fe)");

        accepted = feHandler.receiveEnergy(JOULES_CAPACITY, false);
        assertValueEqual(accepted, 0, "accepted energy (fe)");
        assertValueEqual(joulesContainer.getEnergy(), 0L, "stored energy (joules)");
    }

    // WRAPPING FORGE ENERGY TO STRICT ENERGY

    @Test
    @DisplayName("Test wrapping FE to J against a small empty container, filling to max capacity")
    void testFEAsJoulesFillToMax() {
        EnergyStorage feContainer = new EnergyStorage(FE_CAPACITY, FE_CAPACITY, FE_CAPACITY, 0);
        IStrictEnergyHandler joulesHandler = new ForgeStrictEnergyHandler(feContainer, getConverter(CONVERSION_RATE));

        long extractedJoules = joulesHandler.extractEnergy(FE_CAPACITY, Action.SIMULATE);
        assertValueEqual(extractedJoules, 0L, "extracted energy (joules) from empty");
        extractedJoules = joulesHandler.extractEnergy(FE_CAPACITY, Action.EXECUTE);
        assertValueEqual(extractedJoules, 0L, "extracted energy (joules) from empty");
        assertValueEqual(joulesHandler.getMaxEnergy(0), (long) JOULES_CAPACITY, "max energy (joules)");

        long joulesRemaining = joulesHandler.insertEnergy(JOULES_CAPACITY, Action.SIMULATE);
        assertValueEqual(joulesRemaining, 0L, "remaining inserted energy (joules)");

        joulesRemaining = joulesHandler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(joulesRemaining, 0L, "remaining inserted energy (joules)");
        assertValueEqual(joulesHandler.getEnergy(0), (long) JOULES_CAPACITY, "stored energy");
    }

    @Test
    @DisplayName("Test wrapping FE to J against a small full container")
    void testFEAsJoules() {
        EnergyStorage feStorage = new EnergyStorage(JOULES_CAPACITY, JOULES_CAPACITY, JOULES_CAPACITY, JOULES_CAPACITY);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(feStorage, getConverter(CONVERSION_RATE));

        long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.SIMULATE);
        assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
        remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
        long extracted = handler.extractEnergy(JOULES_CAPACITY, Action.SIMULATE);
        assertValueEqual(extracted, (long) JOULES_CAPACITY, "extracted energy");
        extracted = handler.extractEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(extracted, (long) JOULES_CAPACITY, "extracted energy");
        assertValueEqual(handler.getEnergy(0), (long) (CONVERSION_RATE * JOULES_CAPACITY) - JOULES_CAPACITY, "stored energy");
    }

    @Test
    @DisplayName("Test wrapping FE to J against a small nearly full container with 1 rf missing")
    void testFEAsJoulesNearlyFull() {
        //simulates BasicInventorySlot
        EnergyStorage feStorage = new EnergyStorage(FE_CAPACITY, FE_CAPACITY, FE_CAPACITY, FE_CAPACITY - 1);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(feStorage, getConverter(CONVERSION_RATE));

        long storedEnergy = JOULES_CAPACITY;
        long simulatedRemainder = handler.insertEnergy(storedEnergy, Action.SIMULATE);
        long executedRemainder = handler.insertEnergy(storedEnergy, Action.EXECUTE);
        Assertions.assertEquals(simulatedRemainder, executedRemainder, "simulate and execute should be the same");

        Assertions.assertEquals(storedEnergy, simulatedRemainder, "expected conversion fail due to floating point remainder");
    }

    //Validate behavior for when the conversion is the inverse of the default

    // WRAPPING STRICT ENERGY TO FORGE ENERGY

    @Test
    @DisplayName("Test wrapping J to FE (inverse conversion) against a small empty container")
    void testJoulesAsFE1() {
        IEnergyContainer container = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        container.setEnergy(0);
        IEnergyStorage handler = createForgeWrappedStrictEnergyHandler(container, INVERSE_CONVERSION);
        int extracted = handler.extractEnergy(JOULES_CAPACITY, false);
        assertValueEqual(extracted, 0, "extracted energy");
        assertValueEqual(handler.getMaxEnergyStored(), (int) (CONVERSION_RATE * JOULES_CAPACITY), "max energy");
        //Actual capacity in FE is 2,500
        int accepted = handler.receiveEnergy(JOULES_CAPACITY, false);
        assertValueEqual(accepted, JOULES_CAPACITY, "accepted energy");
        assertValueEqual(handler.getEnergyStored(), JOULES_CAPACITY, "stored energy");
    }

    @Test
    @DisplayName("Test wrapping J to FE (inverse conversion) against a small full container")
    void testJoulesAsFE2() {
        IEnergyContainer container = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        container.setEnergy(JOULES_CAPACITY);
        IEnergyStorage handler = createForgeWrappedStrictEnergyHandler(container, INVERSE_CONVERSION);
        int accepted = handler.receiveEnergy(JOULES_CAPACITY, false);
        assertValueEqual(accepted, 0, "accepted energy");
        int extracted = handler.extractEnergy(JOULES_CAPACITY, false);
        assertValueEqual(extracted, JOULES_CAPACITY, "extracted energy");
        assertValueEqual(handler.getEnergyStored(), (int) (CONVERSION_RATE * JOULES_CAPACITY) - JOULES_CAPACITY, "stored energy");
    }

    // WRAPPING FORGE ENERGY TO STRICT ENERGY

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) against a small empty container")
    void testFEAsJoules3() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(0, JOULES_CAPACITY, INVERSE_CONVERSION);
        long extracted = handler.extractEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(extracted, 0L, "extracted energy");
        long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(remainder, (long) JOULES_CAPACITY - FE_CAPACITY, "remaining inserted energy");
        assertValueEqual(handler.getEnergy(0), (long) FE_CAPACITY, "stored energy");
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) against a small full container")
    void testFEAsJoules4() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(JOULES_CAPACITY, JOULES_CAPACITY, INVERSE_CONVERSION);
        long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
        long extracted = handler.extractEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(extracted, (long) FE_CAPACITY, "extracted energy");
        assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) inserting against a small nearly full container")
    void testFEAsJoules5() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(JOULES_CAPACITY - 2, JOULES_CAPACITY, INVERSE_CONVERSION);//There shouldn't be any room for it
        long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
        //Note: Needed energy should be 1 even though we can't accept it
        assertValueEqual(handler.getEnergy(0), (long) FE_CAPACITY - 1, "stored energy");
        assertValueEqual(handler.getMaxEnergy(0), (long) FE_CAPACITY, "max energy");
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) extracting against a small nearly empty container")
    void testFEAsJoules6() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(2, JOULES_CAPACITY, INVERSE_CONVERSION);//There shouldn't be enough to get a single unit out
        long extracted = handler.extractEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(extracted, 0L, "extracted energy");
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) inserting against a sub one sized container")
    void testFEAsJoules7() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(0, 2, INVERSE_CONVERSION);//There shouldn't be any room for it
        long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
        assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) against a small empty container")
    void testFEAsJoules8() {
        IEnergyStorage container = new EnergyStorage(JOULES_CAPACITY);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container, getConverter(INVERSE_CONVERSION));
        long remainder = handler.insertEnergy(1, Action.EXECUTE);
        assertValueEqual(remainder, 1L, "remaining inserted energy");
        assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
        assertValueEqual(container.getEnergyStored(), 0, "raw stored energy");
    }

    //Validate behavior for when the conversion is 1:1

    // WRAPPING STRICT ENERGY TO FORGE ENERGY

    @Test
    @DisplayName("Test wrapping J to FE (1:1)")
    void testJoulesAsFE9() {
        IEnergyContainer container = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        IEnergyStorage handler = createForgeWrappedStrictEnergyHandler(container, 1D);
        int accepted = handler.receiveEnergy(100, false);
        assertValueEqual(accepted, 100, "accepted energy");
        assertValueEqual(handler.getEnergyStored(), 100, "stored energy");
        assertValueEqual(container.getEnergy(), 100L, "raw stored energy");

        int extracted = handler.extractEnergy(100, false);
        assertValueEqual(extracted, 100, "extracted energy");
        assertValueEqual(handler.getEnergyStored(), 0, "stored energy");
        assertValueEqual(container.getEnergy(), 0L, "raw stored energy");
    }

    @Test
    @DisplayName("Test wrapping J to FE (1:1) having more energy than fits in an int")
    void testJoulesAsFE10() {
        IEnergyContainer container = BasicEnergyContainer.create(4_000_000_000L, null);
        container.setEnergy(3_000_000_000L);
        IEnergyStorage handler = createForgeWrappedStrictEnergyHandler(container, 1D);
        assertValueEqual(handler.getEnergyStored(), Integer.MAX_VALUE, "stored energy");
        int accepted = handler.receiveEnergy(100, false);
        assertValueEqual(accepted, 100, "accepted energy");
        assertValueEqual(handler.getEnergyStored(), Integer.MAX_VALUE, "stored energy");
        assertValueEqual(container.getEnergy(), 3_000_000_100L, "raw stored energy");

        int extracted = handler.extractEnergy(100, false);
        assertValueEqual(extracted, 100, "extracted energy");
        assertValueEqual(handler.getEnergyStored(), Integer.MAX_VALUE, "stored energy");
        assertValueEqual(container.getEnergy(), 3_000_000_000L, "raw stored energy");
    }

    // WRAPPING FORGE ENERGY TO STRICT ENERGY

    @Test
    @DisplayName("Test wrapping FE to J (1:1)")
    void testFEAsJoules11() {
        IEnergyStorage container = new EnergyStorage(JOULES_CAPACITY);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container, getConverter(1D));
        long remainder = handler.insertEnergy(100, Action.EXECUTE);
        assertValueEqual(remainder, 0L, "remaining inserted energy");
        assertValueEqual(handler.getEnergy(0), 100L, "stored energy");
        assertValueEqual(container.getEnergyStored(), 100, "raw stored energy");

        long extracted = handler.extractEnergy(100, Action.EXECUTE);
        assertValueEqual(extracted, 100L, "extracted energy");
        assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
        assertValueEqual(container.getEnergyStored(), 0, "raw stored energy");
    }

    private IEnergyStorage createForgeWrappedStrictEnergyHandler(IEnergyContainer container, double conversionRate) {
        List<IEnergyContainer> containers = Collections.singletonList(container);
        return new ForgeEnergyIntegration(new IMekanismStrictEnergyHandler() {
            @NotNull
            @Override
            public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
                return containers;
            }

            @Override
            public void onContentsChanged() {
            }
        }, getConverter(conversionRate));
    }

    private static @NotNull IEnergyConversion getConverter(double conversionRate) {
        return new IEnergyConversion() {
            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public double getConversion() {
                return conversionRate;
            }
        };
    }

    //todo remove this and have them check the FE storage too
    private IStrictEnergyHandler createStrictForgeEnergyHandler(int energy, int capacityFE, double conversionRate) {
        return new ForgeStrictEnergyHandler(new EnergyStorage(capacityFE, capacityFE, capacityFE, energy), getConverter(conversionRate));
    }

    private static void assertValueEqual(Object actual, Object expected, String valueName) {
        Assertions.assertEquals(expected, actual, valueName);
    }

}
