package mekanism.common.tests.energy;

import mekanism.api.Action;
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
        double defaultConversion = 2.5;
        double inverseConversion = 1 / defaultConversion;
        int capacity = 1_000;
        int convertedCapacity = (int) (capacity / defaultConversion);
        helper.startSequence()
              //Ensure the config starts out at the default value
              .thenExecute(() -> MekanismConfig.general.forgeConversionRate.set(defaultConversion))

              // WRAPPING STRICT ENERGY TO FORGE ENERGY

              //Test against a small empty container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(0, capacity))
              .thenExecute(handler -> {
                  int extracted = handler.extractEnergy(capacity, false);
                  helper.assertValueEqual(extracted, 0, "extracted energy");
                  int accepted = handler.receiveEnergy(capacity, false);
                  helper.assertValueEqual(accepted, convertedCapacity, "accepted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), convertedCapacity, "stored energy");
              })
              //Test against a small full container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(capacity, capacity))
              .thenExecute(handler -> {
                  int accepted = handler.receiveEnergy(capacity, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy");
                  int extracted = handler.extractEnergy(capacity, false);
                  helper.assertValueEqual(extracted, convertedCapacity, "extracted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), 0, "stored energy");
              })
              //Test inserting against a small nearly full container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(capacity - 2, capacity))
              .thenExecute(handler -> {//There shouldn't be any room for it
                  int accepted = handler.receiveEnergy(capacity, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy");
                  //Note: Needed energy should be 1 even though we can't accept it
                  helper.assertValueEqual(handler.getEnergyStored(), convertedCapacity - 1, "stored energy");
                  helper.assertValueEqual(handler.getMaxEnergyStored(), convertedCapacity, "max energy");
              })
              //Test extracting against a small nearly empty container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(2, capacity))
              .thenExecute(handler -> {//There shouldn't be enough to get a single unit out
                  int extracted = handler.extractEnergy(capacity, false);
                  helper.assertValueEqual(extracted, 0, "extracted energy");
              })
              //Test inserting against a sub one sized container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(0, 2))
              .thenExecute(handler -> {//There shouldn't be any room for it
                  int accepted = handler.receiveEnergy(capacity, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy");
              })
              //Test against a small empty container
              .thenMap(() -> BasicEnergyContainer.create(capacity, null))
              .thenExecute(container -> {
                  IEnergyStorage handler = helper.createForgeWrappedStrictEnergyHandler(container);
                  int accepted = handler.receiveEnergy(1, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), 0, "stored energy");
                  helper.assertValueEqual(container.getEnergy(), 0L, "raw stored energy");
              })

              // WRAPPING FORGE ENERGY TO STRICT ENERGY

              //Test against a small empty container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(0, capacity))
              .thenExecute(handler -> {
                  long extracted = handler.extractEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(extracted, 0L, "extracted energy");
                  helper.assertValueEqual(handler.getMaxEnergy(0), (long) (defaultConversion * capacity), "max energy");
                  //Actual capacity in Joules is 2,500
                  long remainder = handler.insertEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(remainder, 0L, "remaining inserted energy");
                  helper.assertValueEqual(handler.getEnergy(0), (long) capacity, "stored energy");
              })
              //Test against a small full container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(capacity, capacity))
              .thenExecute(handler -> {
                  long remainder = handler.insertEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) capacity, "remaining inserted energy");
                  long extracted = handler.extractEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(extracted, (long) capacity, "extracted energy");
                  helper.assertValueEqual(handler.getEnergy(0), (long) (defaultConversion * capacity) - capacity, "stored energy");
              })

              //Validate behavior for when the conversion is the inverse of the default
              .thenExecute(() -> MekanismConfig.general.forgeConversionRate.set(inverseConversion))
              // WRAPPING STRICT ENERGY TO FORGE ENERGY

              //Test against a small empty container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(0, capacity))
              .thenExecute(handler -> {
                  int extracted = handler.extractEnergy(capacity, false);
                  helper.assertValueEqual(extracted, 0, "extracted energy");
                  helper.assertValueEqual(handler.getMaxEnergyStored(), (int) (defaultConversion * capacity), "max energy");
                  //Actual capacity in FE is 2,500
                  int accepted = handler.receiveEnergy(capacity, false);
                  helper.assertValueEqual(accepted, capacity, "accepted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), capacity, "stored energy");
              })
              //Test against a small full container
              .thenMap(() -> helper.createForgeWrappedStrictEnergyHandler(capacity, capacity))
              .thenExecute(handler -> {
                  int accepted = handler.receiveEnergy(capacity, false);
                  helper.assertValueEqual(accepted, 0, "accepted energy");
                  int extracted = handler.extractEnergy(capacity, false);
                  helper.assertValueEqual(extracted, capacity, "extracted energy");
                  helper.assertValueEqual(handler.getEnergyStored(), (int) (defaultConversion * capacity) - capacity, "stored energy");
              })

              // WRAPPING FORGE ENERGY TO STRICT ENERGY

              //Test against a small empty container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(0, capacity))
              .thenExecute(handler -> {
                  long extracted = handler.extractEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(extracted, 0L, "extracted energy");
                  long remainder = handler.insertEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) capacity - convertedCapacity, "remaining inserted energy");
                  helper.assertValueEqual(handler.getEnergy(0), (long) convertedCapacity, "stored energy");
              })
              //Test against a small full container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(capacity, capacity))
              .thenExecute(handler -> {
                  long remainder = handler.insertEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) capacity, "remaining inserted energy");
                  long extracted = handler.extractEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(extracted, (long) convertedCapacity, "extracted energy");
                  helper.assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
              })
              //Test inserting against a small nearly full container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(capacity - 2, capacity))
              .thenExecute(handler -> {//There shouldn't be any room for it
                  long remainder = handler.insertEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) capacity, "remaining inserted energy");
                  //Note: Needed energy should be 1 even though we can't accept it
                  helper.assertValueEqual(handler.getEnergy(0), (long) convertedCapacity - 1, "stored energy");
                  helper.assertValueEqual(handler.getMaxEnergy(0), (long) convertedCapacity, "max energy");
              })
              //Test extracting against a small nearly empty container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(2, capacity))
              .thenExecute(handler -> {//There shouldn't be enough to get a single unit out
                  long extracted = handler.extractEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(extracted, 0L, "extracted energy");
              })
              //Test inserting against a sub one sized container
              .thenMap(() -> helper.createStrictForgeEnergyHandler(0, 2))
              .thenExecute(handler -> {//There shouldn't be any room for it
                  long remainder = handler.insertEnergy(capacity, Action.EXECUTE);
                  helper.assertValueEqual(remainder, (long) capacity, "remaining inserted energy");
              })
              //Test against a small empty container
              .thenMap(() -> new EnergyStorage(capacity))
              .thenExecute(container -> {
                  IStrictEnergyHandler handler = new ForgeStrictEnergyHandler(container);
                  long remainder = handler.insertEnergy(1, Action.EXECUTE);
                  helper.assertValueEqual(remainder, 1L, "remaining inserted energy");
                  helper.assertValueEqual(handler.getEnergy(0), 0L, "stored energy");
                  helper.assertValueEqual(container.getEnergyStored(), 0, "raw stored energy");
              })


              //Validate behavior for when the conversion is 1:1
              .thenExecute(() -> MekanismConfig.general.forgeConversionRate.set(1))



              //TODO: Implement me
              //TODO: Tests for when we are > max int

              //Reset to default config so that we make sure it is at the value we expect and no saving happens
              .thenExecute(() -> MekanismConfig.general.forgeConversionRate.set(defaultConversion))
              .thenSucceed();
    }
}