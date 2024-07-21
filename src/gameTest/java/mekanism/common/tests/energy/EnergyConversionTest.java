package mekanism.common.tests.energy;

import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.energy.forgeenergy.ForgeStrictEnergyHandler;
import mekanism.common.tests.helpers.EnergyTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "energy.conversion")
public class EnergyConversionTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that energy conversion works properly with various decimal values.")
    public static void testForgeEnergy(final EnergyTestHelper helper) {
        final double CONVERSION_RATE = 2.5;
        final double INVERSE_CONVERSION = 1 / CONVERSION_RATE;
        int JOULES_CAPACITY = 1_000;
        int FE_CAPACITY = 400; // capacity / CONVERSION_RATE
        helper.startSequence()
              //Ensure the config starts out at the default value
              .thenExecute(() -> MekanismConfig.general.forgeConversionRate.set(CONVERSION_RATE))

              // WRAPPING STRICT ENERGY TO FORGE ENERGY

              //Test against a small empty container
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  //sanity check nothing can be extracted
                  int extracted = feHandler.extractEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(extracted, 0, "extracted energy (fe)");
                  helper.assertValueEqual(feHandler.getMaxEnergyStored(), FE_CAPACITY, "FE capacity");

                  //insert more than the FE capacity, check it capped at FE max
                  int accepted = feHandler.receiveEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(accepted, FE_CAPACITY, "Accepted FE");
                  helper.assertValueEqual(feHandler.getEnergyStored(), FE_CAPACITY, "stored energy (fe)");
                  helper.assertValueEqual(joulesContainer.getEnergy(), (long) JOULES_CAPACITY, "stored energy (joules)");
              })

              //Test against a small empty container, rejecting sub-unit values
              .thenMap(() -> BasicEnergyContainer.create(JOULES_CAPACITY, null))
              .thenExecute(joulesContainer -> {
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);
                  int accepted = feHandler.receiveEnergy(1, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy");
                  helper.assertValueEqual(feHandler.getEnergyStored(), 0, "stored energy");
                  helper.assertValueEqual(joulesContainer.getEnergy(), 0L, "raw stored energy");
              })

              //Test against a small full container
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
                  joulesContainer.setEnergy(JOULES_CAPACITY);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  //try to insert to full container
                  int accepted = feHandler.receiveEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy when full");

                  //extract beyond converted capacity
                  int extractedFE = feHandler.extractEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(extractedFE, FE_CAPACITY, "extracted energy (fe)");
                  helper.assertValueEqual(feHandler.getEnergyStored(), 0, "stored energy (fe)");
                  helper.assertValueEqual(joulesContainer.getEnergy(), 0L, "stored energy (joules)");
              })

              //Test inserting against a small nearly full container (which cant fit full conversion)
              //There shouldn't be any room for it
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
                  joulesContainer.setEnergy(JOULES_CAPACITY - 2);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  //sanity check
                  //Note: Needed energy should be 1 even though we can't accept it

                  helper.assertValueEqual(feHandler.getEnergyStored(), FE_CAPACITY - 1, "stored energy");
                  helper.assertValueEqual(feHandler.getMaxEnergyStored(), FE_CAPACITY, "max energy");

                  int accepted = feHandler.receiveEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy (fe)");
                  helper.assertValueEqual(joulesContainer.getEnergy(), (long) JOULES_CAPACITY - 2, "joules contents");
              })

              //Test inserting against a small nearly full container (with enough for just over the conversion rate)
              //There should be a partial store
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
                  joulesContainer.setEnergy(997);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  //sanity check.
                  helper.assertValueEqual(feHandler.getEnergyStored(), 398, "stored energy");

                  int accepted = feHandler.receiveEnergy(1000, false);
                  helper.assertValueEqual(joulesContainer.getEnergy(), 997L, "stored joules after insert");
                  helper.assertValueEqual(accepted, 0, "accepted energy (fe)");
              })

              //Test extracting against a small nearly empty container (less than conversion rate)
              //There shouldn't be enough to get a single unit out
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
                  joulesContainer.setEnergy(2);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  int extracted = feHandler.extractEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(extracted, 0, "extracted energy (fe)");
                  helper.assertValueEqual(joulesContainer.getEnergy(), 2L, "stored energy (joules)");
              })

              //Test inserting against a small nearly full container (with enough for two full and one partial unit 8J)
              //There should be a partial store
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
                  joulesContainer.setEnergy(992);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  //sanity check.
                  helper.assertValueEqual(feHandler.getEnergyStored(), 396, "stored energy");

                  int accepted = feHandler.receiveEnergy(1000, false);
                  helper.assertValueEqual(joulesContainer.getEnergy(), 997L, "stored joules after insert");
                  helper.assertValueEqual(accepted, 2, "accepted energy (fe)");
              })

              //Test inserting against a small empty container with an uneven insert
              //There should be a partial store
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(1000, null);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  //sanity check.
                  helper.assertValueEqual(feHandler.getEnergyStored(), 0, "stored energy (fe)");

                  int accepted = feHandler.receiveEnergy(3, false);
                  helper.assertValueEqual(joulesContainer.getEnergy(), 5L, "stored joules after insert");
                  helper.assertValueEqual(accepted, 2, "accepted energy (fe)");
              })

              //Test extracting against a small nearly empty container (2.5x conversion)
              //There should be enough to get 2 converted units out
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(JOULES_CAPACITY, null);
                  joulesContainer.setEnergy(8);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  int extracted = feHandler.extractEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(extracted, 2, "extracted energy (fe)");
                  helper.assertValueEqual(joulesContainer.getEnergy(), 2L, "stored energy (joules)");
              })

              //Test inserting against a sub one sized container
              //There shouldn't be any room for it
              .thenExecute(() -> {
                  IEnergyContainer joulesContainer = BasicEnergyContainer.create(2, null);
                  joulesContainer.setEnergy(0);
                  IEnergyStorage feHandler = helper.createForgeWrappedStrictEnergyHandler(joulesContainer);

                  int accepted = feHandler.receiveEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy (fe)");
                  helper.assertValueEqual(joulesContainer.getEnergy(), 0L, "stored energy (joules)");
              })

              // WRAPPING FORGE ENERGY TO STRICT ENERGY

              //Test against a small empty container, filling to max capacity
              .thenExecute(() -> {
                  EnergyStorage feContainer = new EnergyStorage(FE_CAPACITY, FE_CAPACITY, FE_CAPACITY, 0);
                  IStrictEnergyHandler joulesHandler = new ForgeStrictEnergyHandler(feContainer);

                  long extractedJoules = joulesHandler.extractEnergy(FE_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(extractedJoules, 0L, "extracted energy (joules) from empty");
                  helper.assertValueEqual(joulesHandler.getMaxEnergy(0), (long) JOULES_CAPACITY, "max energy (joules)");

                  long joulesRemaining = joulesHandler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(joulesRemaining, 0L, "remaining inserted energy (joules)");
                  helper.assertValueEqual(joulesHandler.getEnergy(0), (long) JOULES_CAPACITY, "stored energy");
              })

              //Test against a small full container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(JOULES_CAPACITY, JOULES_CAPACITY))
              .thenExecute(handler -> {
                  long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
                  long extracted = handler.extractEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(extracted, (long) JOULES_CAPACITY, "extracted energy");
                  helper.assertValueEqual(handler.getEnergy(0), (long) (CONVERSION_RATE * JOULES_CAPACITY) - JOULES_CAPACITY, "stored energy");
              })

              //Validate behavior for when the conversion is the inverse of the default
              .thenExecute(() -> MekanismConfig.general.forgeConversionRate.set(INVERSE_CONVERSION))
              // WRAPPING STRICT ENERGY TO FORGE ENERGY

              //Test against a small empty container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(0, JOULES_CAPACITY))
              .thenExecute(handler -> {
                  int extracted = handler.extractEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(extracted, 0, "extracted energy");
                  helper.assertValueEqual(handler.getMaxEnergyStored(), (int) (CONVERSION_RATE * JOULES_CAPACITY), "max energy");
                  //Actual capacity in FE is 2,500
                  int accepted = handler.receiveEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(accepted, JOULES_CAPACITY, "accepted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), JOULES_CAPACITY, "stored energy");
              })
              //Test against a small full container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(JOULES_CAPACITY, JOULES_CAPACITY))
              .thenExecute(handler -> {
                  int accepted = handler.receiveEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy");
                  int extracted = handler.extractEnergy(JOULES_CAPACITY, false);
                  helper.assertValueEqual(extracted, JOULES_CAPACITY, "extracted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), (int) (CONVERSION_RATE * JOULES_CAPACITY) - JOULES_CAPACITY, "stored energy");
              })

              // WRAPPING FORGE ENERGY TO STRICT ENERGY

              //Test against a small empty container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(0, JOULES_CAPACITY))
              .thenExecute(handler -> {
                  long extracted = handler.extractEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(extracted, 0L, "extracted energy");
                  long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) JOULES_CAPACITY - FE_CAPACITY, "remaining inserted energy");
                  helper.assertValueEqual(handler.getEnergy(0), (long) FE_CAPACITY, "stored energy");
              })
              //Test against a small full container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(JOULES_CAPACITY, JOULES_CAPACITY))
              .thenExecute(handler -> {
                  long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
                  long extracted = handler.extractEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(extracted, (long) FE_CAPACITY, "extracted energy");
                  helper.assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
              })
              //Test inserting against a small nearly full container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(JOULES_CAPACITY - 2, JOULES_CAPACITY))
              .thenExecute(handler -> {//There shouldn't be any room for it
                  long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
                  //Note: Needed energy should be 1 even though we can't accept it
                  helper.assertValueEqual(handler.getEnergy(0), (long) FE_CAPACITY - 1, "stored energy");
                  helper.assertValueEqual(handler.getMaxEnergy(0), (long) FE_CAPACITY, "max energy");
              })
              //Test extracting against a small nearly empty container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(2, JOULES_CAPACITY))
              .thenExecute(handler -> {//There shouldn't be enough to get a single unit out
                  long extracted = handler.extractEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(extracted, 0L, "extracted energy");
              })
              //Test inserting against a sub one sized container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(0, 2))
              .thenExecute(handler -> {//There shouldn't be any room for it
                  long remainder = handler.insertEnergy(JOULES_CAPACITY, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) JOULES_CAPACITY, "remaining inserted energy");
              })
              //Test against a small empty container
              .thenMap(() -> new EnergyStorage(JOULES_CAPACITY))
              .thenExecute(container -> {
                  IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container);
                  long remainder = handler.insertEnergy(1, Action.EXECUTE);
                  helper.assertValueEqual(remainder, 1L, "remaining inserted energy");
                  helper.assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
                  helper.assertValueEqual(container.getEnergyStored(), 0, "raw stored energy");
              })


              //Validate behavior for when the conversion is 1:1
              .thenExecute(() -> MekanismConfig.general.forgeConversionRate.set(1))

              // WRAPPING STRICT ENERGY TO FORGE ENERGY

              .thenMap(() -> BasicEnergyContainer.create(JOULES_CAPACITY, null))
              .thenExecute(container -> {
                  IEnergyStorage handler = helper.createForgeWrappedStrictEnergyHandler(container);
                  int accepted = handler.receiveEnergy(100, false);
                  helper.assertValueEqual(accepted, 100, "accepted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), 100, "stored energy");
                  helper.assertValueEqual(container.getEnergy(), 100L, "raw stored energy");

                  int extracted = handler.extractEnergy(100, false);
                  helper.assertValueEqual(extracted, 100, "extracted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), 0, "stored energy");
                  helper.assertValueEqual(container.getEnergy(), 0L, "raw stored energy");
              })
              //Test having more energy than fits in an int
              .thenMap(() -> BasicEnergyContainer.create(4_000_000_000L, null))
              .thenExecute(container -> {
                  container.setEnergy(3_000_000_000L);
                  IEnergyStorage handler = helper.createForgeWrappedStrictEnergyHandler(container);
                  helper.assertValueEqual(handler.getEnergyStored(), Integer.MAX_VALUE, "stored energy");
                  int accepted = handler.receiveEnergy(100, false);
                  helper.assertValueEqual(accepted, 100, "accepted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), Integer.MAX_VALUE, "stored energy");
                  helper.assertValueEqual(container.getEnergy(), 3_000_000_100L, "raw stored energy");

                  int extracted = handler.extractEnergy(100, false);
                  helper.assertValueEqual(extracted, 100, "extracted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), Integer.MAX_VALUE, "stored energy");
                  helper.assertValueEqual(container.getEnergy(), 3_000_000_000L, "raw stored energy");
              })

              // WRAPPING FORGE ENERGY TO STRICT ENERGY

              .thenMap(() -> new EnergyStorage(JOULES_CAPACITY))
              .thenExecute(container -> {
                  IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container);
                  long remainder = handler.insertEnergy(100, Action.EXECUTE);
                  helper.assertValueEqual(remainder, 0L, "remaining inserted energy");
                  helper.assertValueEqual(handler.getEnergy(0), 100L, "stored energy");
                  helper.assertValueEqual(container.getEnergyStored(), 100, "raw stored energy");

                  long extracted = handler.extractEnergy(100, Action.EXECUTE);
                  helper.assertValueEqual(extracted, 100L, "extracted energy");
                  helper.assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
                  helper.assertValueEqual(container.getEnergyStored(), 0, "raw stored energy");
              })

              //Reset to default config so that we make sure it is at the value we expect and no saving happens
              .thenExecute(() -> MekanismConfig.general.forgeConversionRate.set(CONVERSION_RATE))
              .thenSucceed();
    }
}