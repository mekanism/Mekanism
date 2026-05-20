package mekanism.common.integration.energy.forgeenergy;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.NonNull;
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

        try (Transaction transaction = Transaction.openRoot()) {
            //sanity check nothing can be extracted
            int extracted = feHandler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, extracted, "extracted energy (fe)");
            Assertions.assertEquals(FE_CAPACITY, feHandler.getCapacityAsInt(), "FE capacity");

            //insert more than the FE capacity, check it capped at FE max
            int inserted = feHandler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(FE_CAPACITY, inserted, "Inserted FE");
            Assertions.assertEquals(FE_CAPACITY, feHandler.getAmountAsInt(), "stored energy (fe)");
            Assertions.assertEquals(JOULES_CAPACITY, joulesContainer.energy(), "stored energy (joules)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE against a small empty container, rejecting sub-unit values")
    void testJoulesAsFESmallEmptySubUnit() {
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);

        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = feHandler.insert(1, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy");
            Assertions.assertEquals(0, feHandler.getAmountAsInt(), "stored energy");
            Assertions.assertEquals(0L, joulesContainer.energy(), "raw stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE against a small full container")
    void testJoulesAsFESmallFull() {
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction transaction = Transaction.openRoot()) {
            joulesContainer.setEnergy(JOULES_CAPACITY, transaction);
            //try to insert to full container
            int inserted = feHandler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy when full");

            //extract beyond converted capacity
            int extractedFE = feHandler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(FE_CAPACITY, extractedFE, "extracted energy (fe)");
            Assertions.assertEquals(0, feHandler.getAmountAsInt(), "stored energy (fe)");
            Assertions.assertEquals(0L, joulesContainer.energy(), "stored energy (joules)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (which cant fit full conversion)")
    void testJoulesAsFENearlyFullNoAccept() {
        //Note: There shouldn't be any room for it
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction transaction = Transaction.openRoot()) {
            joulesContainer.setEnergy(JOULES_CAPACITY - 2, transaction);

            //sanity check
            //Note: Needed energy should be 1 even though we can't accept it

            Assertions.assertEquals(FE_CAPACITY - 1, feHandler.getAmountAsInt(), "stored energy");
            Assertions.assertEquals(FE_CAPACITY, feHandler.getCapacityAsInt(), "max energy");

            int inserted = feHandler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy (fe)");
            Assertions.assertEquals(JOULES_CAPACITY - 2, joulesContainer.energy(), "joules contents");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (with enough for just over the conversion rate)")
    void testJoulesAsFEFullContainerPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction transaction = Transaction.openRoot()) {
            joulesContainer.setEnergy(997, transaction);
            //sanity check.
            Assertions.assertEquals(398, feHandler.getAmountAsInt(), "stored energy");
            int inserted = feHandler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy (fe)");
            Assertions.assertEquals(997L, joulesContainer.energy(), "stored joules after insert");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE extracting against a small nearly empty container (less than conversion rate)")
    void testJoulesAsFEExtractNotEnough() {
        //Note: There shouldn't be enough to get a single unit out
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction transaction = Transaction.openRoot()) {
            joulesContainer.setEnergy(2, transaction);
            int extracted = feHandler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, extracted, "extracted energy (fe)");
            Assertions.assertEquals(2L, joulesContainer.energy(), "stored energy (joules)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small nearly full container (with enough for two full and one partial unit 8J)")
    void testJoulesAsFEInsertPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction transaction = Transaction.openRoot()) {
            joulesContainer.setEnergy(992, transaction);
            //sanity check.
            Assertions.assertEquals(396, feHandler.getAmountAsInt(), "stored energy");
            int inserted = feHandler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(2, inserted, "inserted energy (fe)");
            Assertions.assertEquals(997L, joulesContainer.energy(), "stored joules after insert");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a small empty container with an uneven insert")
    void testJoulesAsFEEmptyContainerPartialStore() {
        //Note: There should be a partial store
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        //sanity check.
        Assertions.assertEquals(0, feHandler.getAmountAsInt(), "stored energy (fe)");

        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = feHandler.insert(3, transaction);
            Assertions.assertEquals(5L, joulesContainer.energy(), "stored joules after insert");
            Assertions.assertEquals(2, inserted, "inserted energy (fe)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE extracting against a small nearly empty container (2.5x conversion)")
    void testJoulesAsFEExtractPartial() {
        //Note: There should be enough to get 2 converted units out
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction transaction = Transaction.openRoot()) {
            joulesContainer.setEnergy(8, transaction);
            int extracted = feHandler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(2, extracted, "extracted energy (fe)");
            Assertions.assertEquals(3L, joulesContainer.energy(), "stored energy (joules)");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE inserting against a sub one sized container")
    void testJoulesAsFECantFit() {
        //Note: There shouldn't be any room for it
        IEnergyContainer joulesContainer = BasicEnergyContainer.create(2, null);
        EnergyHandler feHandler = createForgeWrappedStrictEnergyHandler(joulesContainer, CONVERSION_RATE);

        try (Transaction transaction = Transaction.openRoot()) {
            joulesContainer.setEnergy(0, transaction);
            int inserted = feHandler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy (fe)");
            Assertions.assertEquals(0L, joulesContainer.energy(), "stored energy (joules)");
        }
    }

    // WRAPPING FORGE ENERGY TO STRICT ENERGY

    @Test
    @DisplayName("Test wrapping FE to J against a small empty container, filling to max capacity")
    void testFEAsJoulesFillToMax() {
        EnergyHandler feContainer = new SimpleEnergyHandler(FE_CAPACITY, FE_CAPACITY, FE_CAPACITY, 0);
        IStrictEnergyHandler joulesHandler = new ForgeStrictEnergyHandler(feContainer, getConverter(CONVERSION_RATE));

        try (Transaction transaction = Transaction.openRoot()) {
            long extractedJoules = joulesHandler.extract(FE_CAPACITY, transaction);
            Assertions.assertEquals(0L, extractedJoules, "extracted energy (joules) from empty");
            Assertions.assertEquals(JOULES_CAPACITY, joulesHandler.getCapacityAsLong(0), "max energy (joules)");

            long joulesInserted = joulesHandler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(JOULES_CAPACITY, joulesInserted, "remaining inserted energy (joules)");
            Assertions.assertEquals(JOULES_CAPACITY, joulesHandler.getAmountAsLong(0), "stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping FE to J against a small full container")
    void testFEAsJoules() {
        EnergyHandler feStorage = new SimpleEnergyHandler(JOULES_CAPACITY, JOULES_CAPACITY, JOULES_CAPACITY, JOULES_CAPACITY);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(feStorage, getConverter(CONVERSION_RATE));

        try (Transaction transaction = Transaction.openRoot()) {
            long inserted = handler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy");
            long extracted = handler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(JOULES_CAPACITY, extracted, "extracted energy");
            Assertions.assertEquals((long) (CONVERSION_RATE * JOULES_CAPACITY) - JOULES_CAPACITY, handler.getAmountAsLong(0), "stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping FE to J against a small nearly full container with 1 rf missing")
    void testFEAsJoulesNearlyFull() {
        //simulates BasicInventorySlot
        EnergyHandler feStorage = new SimpleEnergyHandler(FE_CAPACITY, FE_CAPACITY, FE_CAPACITY, FE_CAPACITY - 1);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(feStorage, getConverter(CONVERSION_RATE));

        try (Transaction transaction = Transaction.openRoot()) {
            long inserted = handler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "expected conversion fail due to floating point remainder");
        }
    }

    //Validate behavior for when the conversion is the inverse of the default

    // WRAPPING STRICT ENERGY TO FORGE ENERGY

    @Test
    @DisplayName("Test wrapping J to FE (inverse conversion) against a small empty container")
    void testJoulesAsFE1() {
        IEnergyContainer container = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler handler = createForgeWrappedStrictEnergyHandler(container, INVERSE_CONVERSION);
        try (Transaction transaction = Transaction.openRoot()) {
            container.setEnergy(0, transaction);
            int extracted = handler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, extracted, "extracted energy");
            Assertions.assertEquals((int) (CONVERSION_RATE * JOULES_CAPACITY), handler.getCapacityAsInt(), "max energy");
            //Actual capacity in FE is 2,500
            int inserted = handler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(JOULES_CAPACITY, inserted, "inserted energy");
            Assertions.assertEquals(JOULES_CAPACITY, handler.getAmountAsInt(), "stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE (inverse conversion) against a small full container")
    void testJoulesAsFE2() {
        IEnergyContainer container = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler handler = createForgeWrappedStrictEnergyHandler(container, INVERSE_CONVERSION);
        try (Transaction transaction = Transaction.openRoot()) {
            container.setEnergy(JOULES_CAPACITY, transaction);
            int inserted = handler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy");
            int extracted = handler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(JOULES_CAPACITY, extracted, "extracted energy");
            Assertions.assertEquals((int) (CONVERSION_RATE * JOULES_CAPACITY) - JOULES_CAPACITY, handler.getAmountAsInt(), "stored energy");
        }
    }

    // WRAPPING FORGE ENERGY TO STRICT ENERGY

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) against a small empty container")
    void testFEAsJoules3() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(0, JOULES_CAPACITY, INVERSE_CONVERSION);
        try (Transaction transaction = Transaction.openRoot()) {
            long extracted = handler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0L, extracted, "extracted energy");
            long inserted = handler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(FE_CAPACITY, inserted, "inserted energy");
            Assertions.assertEquals(FE_CAPACITY, handler.getAmountAsLong(0), "stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) against a small full container")
    void testFEAsJoules4() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(JOULES_CAPACITY, JOULES_CAPACITY, INVERSE_CONVERSION);
        try (Transaction transaction = Transaction.openRoot()) {
            long inserted = handler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy");
            long extracted = handler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(FE_CAPACITY, extracted, "extracted energy");
            Assertions.assertEquals(0L, handler.getAmountAsLong(0), "stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) inserting against a small nearly full container")
    void testFEAsJoules5() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(JOULES_CAPACITY - 2, JOULES_CAPACITY, INVERSE_CONVERSION);//There shouldn't be any room for it
        try (Transaction transaction = Transaction.openRoot()) {
            long inserted = handler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy");
            //Note: Needed energy should be 1 even though we can't accept it
            Assertions.assertEquals(FE_CAPACITY - 1, handler.getAmountAsLong(0), "stored energy");
            Assertions.assertEquals(FE_CAPACITY, handler.getCapacityAsLong(0), "max energy");
        }
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) extracting against a small nearly empty container")
    void testFEAsJoules6() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(2, JOULES_CAPACITY, INVERSE_CONVERSION);//There shouldn't be enough to get a single unit out
        try (Transaction transaction = Transaction.openRoot()) {
            long extracted = handler.extract(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0L, extracted, "extracted energy");
        }
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) inserting against a sub one sized container")
    void testFEAsJoules7() {
        IStrictEnergyHandler handler = createStrictForgeEnergyHandler(0, 2, INVERSE_CONVERSION);//There shouldn't be any room for it
        try (Transaction transaction = Transaction.openRoot()) {
            long inserted = handler.insert(JOULES_CAPACITY, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy");
        }
    }

    @Test
    @DisplayName("Test wrapping FE to J (inverse conversion) against a small empty container")
    void testFEAsJoules8() {
        EnergyHandler container = new SimpleEnergyHandler(JOULES_CAPACITY);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container, getConverter(INVERSE_CONVERSION));
        try (Transaction transaction = Transaction.openRoot()) {
            long inserted = handler.insert(1, transaction);
            Assertions.assertEquals(0, inserted, "inserted energy");
            Assertions.assertEquals(0L, handler.getAmountAsLong(0), "stored energy");
            Assertions.assertEquals(0, container.getAmountAsInt(), "raw stored energy");
        }
    }

    //Validate behavior for when the conversion is 1:1

    // WRAPPING STRICT ENERGY TO FORGE ENERGY

    @Test
    @DisplayName("Test wrapping J to FE (1:1)")
    void testJoulesAsFE9() {
        IEnergyContainer container = BasicEnergyContainer.create(JOULES_CAPACITY, null);
        EnergyHandler handler = createForgeWrappedStrictEnergyHandler(container, 1D);
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(100, transaction);
            Assertions.assertEquals(100, inserted, "inserted energy");
            Assertions.assertEquals(100, handler.getAmountAsInt(), "stored energy");
            Assertions.assertEquals(100L, container.energy(), "raw stored energy");

            int extracted = handler.extract(100, transaction);
            Assertions.assertEquals(100, extracted, "extracted energy");
            Assertions.assertEquals(0, handler.getAmountAsInt(), "stored energy");
            Assertions.assertEquals(0L, container.energy(), "raw stored energy");
        }
    }

    @Test
    @DisplayName("Test wrapping J to FE (1:1) having more energy than fits in an int")
    void testJoulesAsFE10() {
        IEnergyContainer container = BasicEnergyContainer.create(4_000_000_000L, null);
        EnergyHandler handler = createForgeWrappedStrictEnergyHandler(container, 1D);
        try (Transaction transaction = Transaction.openRoot()) {
            container.setEnergy(3_000_000_000L, transaction);
            Assertions.assertEquals(Integer.MAX_VALUE, handler.getAmountAsInt(), "stored energy");

            int inserted = handler.insert(100, transaction);
            Assertions.assertEquals(100, inserted, "inserted energy");
            Assertions.assertEquals(Integer.MAX_VALUE, handler.getAmountAsInt(), "stored energy");
            Assertions.assertEquals(3_000_000_100L, container.energy(), "raw stored energy");

            int extracted = handler.extract(100, transaction);
            Assertions.assertEquals(100, extracted, "extracted energy");
            Assertions.assertEquals(Integer.MAX_VALUE, handler.getAmountAsInt(), "stored energy");
            Assertions.assertEquals(3_000_000_000L, container.energy(), "raw stored energy");
        }
    }

    // WRAPPING FORGE ENERGY TO STRICT ENERGY

    @Test
    @DisplayName("Test wrapping FE to J (1:1)")
    void testFEAsJoules11() {
        EnergyHandler container = new SimpleEnergyHandler(JOULES_CAPACITY);
        IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container, getConverter(1D));
        try (Transaction transaction = Transaction.openRoot()) {
            long inserted = handler.insert(100, transaction);
            Assertions.assertEquals(100, inserted, "inserted energy");
            Assertions.assertEquals(100, handler.getAmountAsLong(0), "stored energy");
            Assertions.assertEquals(100, container.getAmountAsInt(), "raw stored energy");

            long extracted = handler.extract(100, transaction);
            Assertions.assertEquals(100L, extracted, "extracted energy");
            Assertions.assertEquals(0L, handler.getAmountAsLong(0), "stored energy");
            Assertions.assertEquals(0, container.getAmountAsInt(), "raw stored energy");
        }
    }

    private EnergyHandler createForgeWrappedStrictEnergyHandler(IEnergyContainer container, double conversionRate) {
        List<IEnergyContainer> containers = Collections.singletonList(container);
        return new ForgeEnergyIntegration(new IMekanismStrictEnergyHandler() {
            @NonNull
            @Override
            public List<IEnergyContainer> getContainers() {
                return containers;
            }

            @Override
            public void onContentsChanged() {
            }
        }, getConverter(conversionRate));
    }

    private static @NonNull IEnergyConversion getConverter(double conversionRate) {
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
}
