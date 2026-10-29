package mekanism.common.tests.container;

import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.common.tests.helpers.MekGameTestHelper;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "container.gas_generator")
public class GasGeneratorFuelTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(value = "gas_generator_whole_fuel", description = "A gas generator must store the full energy value of each consumed mB, including near a full buffer.")
    public static void testWholeFuelEnergy(MekGameTestHelper helper) {
        BlockPos relativePos = new BlockPos(0, 0, 0);
        helper.setBlock(relativePos, GeneratorsBlocks.GAS_BURNING_GENERATOR.defaultState());
        TileEntityGasGenerator generator = helper.getBlockEntity(relativePos, TileEntityGasGenerator.class);
        ChemicalResource[] fuels = {
              ChemicalResource.of(helper.getLevel().registryAccess().getOrThrow(ChemicalIds.HYDROGEN)),
              ChemicalResource.of(helper.getLevel().registryAccess().getOrThrow(ChemicalIds.ETHENE))};
        for (ChemicalResource fuel : fuels) {
            int initialFuel = (int) Math.min(4_000, generator.getFuelTank().capacityAsLong(fuel));
            try (Transaction transaction = Transaction.openRoot()) {
                generator.getFuelTank().setContents(fuel, initialFuel, transaction);
                generator.energyContainer().setEnergy(0, transaction);
                transaction.commit();
            }
            int density = generator.getCachedFuel().energyDensity();
            helper.assertTrue(density > 1,
                  "Test requires density > 1 FE/mB to expose rounding waste; density == 1 has no fractional-mB remainder");
            // Check the native rollback contracts used by the generator, without a simulated handler.
            try (Transaction transaction = Transaction.openRoot()) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    helper.assertTrue(generator.energyContainer().insert(density, simulation, AutomationType.INTERNAL) > 0,
                          "Energy preview must exercise a real insertion");
                    helper.assertTrue(generator.getFuelTank().extract(fuel, 1, simulation, AutomationType.INTERNAL) == 1,
                          "Fuel preview must exercise a real extraction");
                }
                helper.assertTrue(generator.energyContainer().getAmountAsLong() == 0 && generator.getFuelTank().amountAsLong() == initialFuel,
                      "Aborting a child transaction must restore both resources");
            }
            try (Transaction transaction = Transaction.openRoot()) {
                helper.assertTrue(generator.energyContainer().insert(density, transaction, AutomationType.INTERNAL) > 0,
                      "Failed-withdrawal check must exercise a real energy insertion");
                ChemicalResource otherFuel = fuel.equals(fuels[0]) ? fuels[1] : fuels[0];
                helper.assertTrue(generator.getFuelTank().extract(otherFuel, 1, transaction, AutomationType.INTERNAL) == 0,
                      "Withdrawing a different fuel must fail");
            }
            helper.assertTrue(generator.energyContainer().getAmountAsLong() == 0 && generator.getFuelTank().amountAsLong() == initialFuel,
                  "Aborting after rejected fuel extraction must not leave free energy");
            long capacity = generator.energyContainer().getCapacityAsLong();
            long[] gaps = {0, 1, density - 1, density, (long) density + 1, 2L * density + 1, capacity / 2, capacity};
            for (long gap : gaps) {
                long beforeEnergy = generator.energyContainer().getCapacityAsLong() - gap;
                try (Transaction transaction = Transaction.openRoot()) {
                    generator.getFuelTank().setContents(fuel, initialFuel, transaction);
                    generator.energyContainer().setEnergy(beforeEnergy, transaction);
                    transaction.commit();
                }
                TileEntityMekanism.tickServer(helper.getLevel(), generator.getBlockPos(), generator.getBlockState(), generator);
                long consumed = initialFuel - generator.getFuelTank().amountAsLong();
                long produced = generator.energyContainer().getAmountAsLong() - beforeEnergy;
                helper.assertTrue(produced == consumed * density,
                      "Stored energy must equal the full value of consumed fuel; gap=" + gap + ", fuel=" + consumed + ", energy=" + produced);
                helper.assertTrue(generator.getActive() == (consumed > 0), "Active state must reflect fuel consumed this tick");
                if (gap < density) {
                    helper.assertTrue(consumed == 0, "Fuel must wait when less than one mB of energy fits");
                } else {
                    helper.assertTrue(produced > 0, "Generator must still generate when a whole mB fits");
                }
            }
            // Consecutive machine-tick calls must conserve resources without resetting the fixture.
            for (int tick = 0; tick < 20; tick++) {
                long beforeEnergy = generator.energyContainer().getAmountAsLong();
                long beforeFuel = generator.getFuelTank().amountAsLong();
                TileEntityMekanism.tickServer(helper.getLevel(), generator.getBlockPos(), generator.getBlockState(), generator);
                long consumed = beforeFuel - generator.getFuelTank().amountAsLong();
                long produced = generator.energyContainer().getAmountAsLong() - beforeEnergy;
                helper.assertTrue(produced == consumed * density, "Consecutive machine ticks must conserve fuel energy");
            }
        }
        helper.succeed();
    }
}
