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
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Forge Energy conversion")
class EnergyConversionTest {//TODO - 26.1: Add tests related to simulating vs actually committing the transactions

    private final double CONVERSION_RATE = 2.5;
    private final double INVERSE_CONVERSION = 1 / CONVERSION_RATE;
    private final int JOULES_CAPACITY = 1_000;
    private final int FE_CAPACITY = 400; // capacity / CONVERSION_RATE

    // WRAPPING STRICT ENERGY TO FORGE ENERGY

    @Test
    @DisplayName("Test wrapping J to FE against a small empty container")
    void testJoulesAsFESmallEmptyToFull() {
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction tx = Transaction.openRoot()) {
            //sanity check nothing can be extracted
            int extracted = feHandler.extract(JOULES_CAPACITY, tx);
            assertValueEqual(extracted, 0, "extracted energy (fe)");
            assertValueEqual(feHandler.getAmountAsInt(), FE_CAPACITY, "FE capacity");

            //insert more than the FE capacity, check it capped at FE max
            int accepted = feHandler.insert(JOULES_CAPACITY, tx);
            assertValueEqual(accepted, FE_CAPACITY, "Accepted FE");
            assertValueEqual(feHandler.getAmountAsInt(), FE_CAPACITY, "stored energy (fe)");
            assertValueEqual(joulesContainer.getEnergy(), (long) JOULES_CAPACITY, "stored energy (joules)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE against a small empty container, rejecting sub-unit values")
    void testJoulesAsFESmallEmptySubUnit() {
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);

        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);
        try (Transaction tx = Transaction.openRoot()) {
            int accepted = feHandler.insert(1, tx);
            assertValueEqual(accepted, 0, "accepted energy");
            assertValueEqual(feHandler.getAmountAsInt(), 0, "stored energy");
            assertValueEqual(joulesContainer.getEnergy(), 0L, "raw stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE against a small full container")
    void testJoulesAsFESmallFull() {
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        joulesContainer.setEnergy(JOULES_CAPACITY);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction tx = Transaction.openRoot()) {
            //try to insert to full container
            int accepted = feHandler.insert(JOULES_CAPACITY, tx);
            assertValueEqual(accepted, 0, "accepted energy when full");
        }

        try (Transaction tx = Transaction.openRoot()) {
            //extract beyond converted capacity
            int extractedFE = feHandler.extract(JOULES_CAPACITY, tx);
            assertValueEqual(extractedFE, FE_CAPACITY, "extracted energy (fe)");
            assertValueEqual(feHandler.getAmountAsInt(), 0, "stored energy (fe)");
            assertValueEqual(joulesContainer.getEnergy(), 0L, "stored energy (joules)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (which cant fit full conversion)")
    void testJoulesAsFENearlyFullNoAccept() {
        //Note: There shouldn't be any room for it
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        joulesContainer.setEnergy(JOULES_CAPACITY - 2);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check
        //Note: Needed energy should be 1 even though we can't accept it

        assertValueEqual(feHandler.getAmountAsInt(), FE_CAPACITY - 1, "stored energy");
        assertValueEqual(feHandler.getCapacityAsInt(), FE_CAPACITY, "max energy");

        try (Transaction tx = Transaction.openRoot()) {
            int accepted = feHandler.insert(JOULES_CAPACITY, tx);
            assertValueEqual(accepted, 0, "accepted energy (fe)");
            assertValueEqual(joulesContainer.getEnergy(), (long) JOULES_CAPACITY - 2, "joules contents");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (with enough for just over the conversion rate)")
    void testJoulesAsFEFullContainerPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
        joulesContainer.setEnergy(997);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check.
        assertValueEqual(feHandler.getAmountAsInt(), 398, "stored energy");

        try (Transaction tx = Transaction.openRoot()) {
            int accepted = feHandler.insert(1000, tx);
            assertValueEqual(accepted, 0, "accepted energy (fe)");
            assertValueEqual(joulesContainer.getEnergy(), 997L, "stored joules after insert");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE extracting against a small nearly empty container (less than conversion rate)")
    void testJoulesAsFEExtractNotEnough() {
        //Note: There shouldn't be enough to get a single unit out
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        joulesContainer.setEnergy(2);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction tx = Transaction.openRoot()) {
            int extracted = feHandler.extract(JOULES_CAPACITY, tx);
            assertValueEqual(extracted, 0, "extracted energy (fe)");
            assertValueEqual(joulesContainer.getEnergy(), 2L, "stored energy (joules)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (with enough for two full and one partial unit 8J)")
    void testJoulesAsFEInsertPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
        joulesContainer.setEnergy(992);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check.
        assertValueEqual(feHandler.getAmountAsInt(), 396, "stored energy");

        try (Transaction tx = Transaction.openRoot()) {
            int accepted = feHandler.insert(1000, tx);
            assertValueEqual(accepted, 2, "accepted energy (fe)");
            assertValueEqual(joulesContainer.getEnergy(), 997L, "stored joules after insert");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small empty container with an uneven insert")
    void testJoulesAsFEEmptyContainerPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check.
        assertValueEqual(feHandler.getAmountAsInt(), 0, "stored energy (fe)");

        try (Transaction tx = Transaction.openRoot()) {
            int accepted = feHandler.insert(3, tx);
            assertValueEqual(joulesContainer.getEnergy(), 5L, "stored joules after insert");
            assertValueEqual(accepted, 2, "accepted energy (fe)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE extracting against a small nearly empty container (2.5x conversion)")
    void testJoulesAsFEExtractPartial() {
        //Note: There should be enough to get 2 converted units out
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        joulesContainer.setEnergy(8);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction tx = Transaction.openRoot()) {
            int extracted = feHandler.extract(JOULES_CAPACITY, tx);
            assertValueEqual(extracted, 2, "extracted energy (fe)");
            assertValueEqual(joulesContainer.getEnergy(), 3L, "stored energy (joules)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a sub one sized container")
    void testJoulesAsFECantFit() {
        //Note: There shouldn't be any room for it
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(2, null);
        joulesContainer.setEnergy(0);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction tx = Transaction.openRoot()) {
            int accepted = feHandler.insert(JOULES_CAPACITY, tx);
            assertValueEqual(accepted, 0, "accepted energy (fe)");
            assertValueEqual(joulesContainer.getEnergy(), 0L, "stored energy (joules)");
        }
    }

    // WRAPPING FORGE ENERGY TO STRICT ENERGY

    @Test
    @DisplayName("Test wrapping FE to J against a small empty container, filling to max capacity")
    void testFEAsJoulesFillToMax() {
        EnergyHandler feContainer = new SimpleEnergyHandler(FE_CAPACITY, FE_CAPACITY, FE_CAPACITY, 0);
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
        EnergyHandler feStorage = new SimpleEnergyHandler(JOULES_CAPACITY, JOULES_CAPACITY, JOULES_CAPACITY, JOULES_CAPACITY);
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
        EnergyHandler feStorage = new SimpleEnergyHandler(FE_CAPACITY, FE_CAPACITY, FE_CAPACITY, FE_CAPACITY - 1);
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
        EnergyHandler handler = createForgeWrappedStrictEnergyHandler(container, INVERSE_CONVERSION);
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(JOULES_CAPACITY, tx);
            assertValueEqual(extracted, 0, "extracted energy");
            assertValueEqual(handler.getCapacityAsInt(), (int) (CONVERSION_RATE * JOULES_CAPACITY), "max energy");
            //Actual capacity in FE is 2,500
            int accepted = handler.insert(JOULES_CAPACITY, tx);
            assertValueEqual(accepted, JOULES_CAPACITY, "accepted energy");
            assertValueEqual(handler.getAmountAsInt(), JOULES_CAPACITY, "stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE (inverse conversion) against a small full container")
    void testJoulesAsFE2() {
        IEnergyContainer container = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        container.setEnergy(JOULES_CAPACITY);
        EnergyHandler handler = createForgeWrappedStrictEnergyHandler(container, INVERSE_CONVERSION);
        try (Transaction tx = Transaction.openRoot()) {
            int accepted = handler.insert(JOULES_CAPACITY, tx);
            assertValueEqual(accepted, 0, "accepted energy");
            int extracted = handler.extract(JOULES_CAPACITY, tx);
            assertValueEqual(extracted, JOULES_CAPACITY, "extracted energy");
            assertValueEqual(handler.getAmountAsInt(), (int) (CONVERSION_RATE * JOULES_CAPACITY) - JOULES_CAPACITY, "stored energy");
        }
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
        EnergyHandler container = new SimpleEnergyHandler(JOULES_CAPACITY);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container, getConverter(INVERSE_CONVERSION));
        long remainder = handler.insertEnergy(1, Action.EXECUTE);
        assertValueEqual(remainder, 1L, "remaining inserted energy");
        assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
        assertValueEqual(container.getAmountAsInt(), 0, "raw stored energy");
    }

    //Validate behavior for when the conversion is 1:1

    // WRAPPING STRICT ENERGY TO FORGE ENERGY

    @Test
    @DisplayName("Test wrapping J to FE (1:1)")
    void testJoulesAsFE9() {
        IEnergyContainer container = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler handler = createForgeWrappedStrictEnergyHandler(container, 1D);
        try (Transaction tx = Transaction.openRoot()) {
            int accepted = handler.insert(100, tx);
            assertValueEqual(accepted, 100, "accepted energy");
            assertValueEqual(handler.getAmountAsInt(), 100, "stored energy");
            assertValueEqual(container.getEnergy(), 100L, "raw stored energy");

            int extracted = handler.extract(100, tx);
            assertValueEqual(extracted, 100, "extracted energy");
            assertValueEqual(handler.getAmountAsInt(), 0, "stored energy");
            assertValueEqual(container.getEnergy(), 0L, "raw stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE (1:1) having more energy than fits in an int")
    void testJoulesAsFE10() {
        IEnergyContainer container = BasicEnergyContainer.create(4_000_000_000L, null);
        container.setEnergy(3_000_000_000L);
        EnergyHandler handler = createForgeWrappedStrictEnergyHandler(container, 1D);
        assertValueEqual(handler.getAmountAsInt(), Integer.MAX_VALUE, "stored energy");
        try (Transaction tx = Transaction.openRoot()) {
            int accepted = handler.insert(100, tx);
            assertValueEqual(accepted, 100, "accepted energy");
            assertValueEqual(handler.getAmountAsInt(), Integer.MAX_VALUE, "stored energy");
            assertValueEqual(container.getEnergy(), 3_000_000_100L, "raw stored energy");

            int extracted = handler.extract(100, tx);
            assertValueEqual(extracted, 100, "extracted energy");
            assertValueEqual(handler.getAmountAsInt(), Integer.MAX_VALUE, "stored energy");
            assertValueEqual(container.getEnergy(), 3_000_000_000L, "raw stored energy");
        }
    }

    // WRAPPING FORGE ENERGY TO STRICT ENERGY

    @Test
    @DisplayName("Test wrapping FE to J (1:1)")
    void testFEAsJoules11() {
        EnergyHandler container = new SimpleEnergyHandler(JOULES_CAPACITY);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container, getConverter(1D));
        long remainder = handler.insertEnergy(100, Action.EXECUTE);
        assertValueEqual(remainder, 0L, "remaining inserted energy");
        assertValueEqual(handler.getEnergy(0), 100L, "stored energy");
        assertValueEqual(container.getAmountAsInt(), 100, "raw stored energy");

        long extracted = handler.extractEnergy(100, Action.EXECUTE);
        assertValueEqual(extracted, 100L, "extracted energy");
        assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
        assertValueEqual(container.getAmountAsInt(), 0, "raw stored energy");
    }

    private EnergyHandler createForgeWrappedStrictEnergyHandler(IEnergyContainer container, double conversionRate) {
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
        return new ForgeStrictEnergyHandler(new SimpleEnergyHandler(capacityFE, capacityFE, capacityFE, energy), getConverter(conversionRate));
    }

    private static void assertValueEqual(Object actual, Object expected, String valueName) {
        Assertions.assertEquals(expected, actual, valueName);
    }

}
