package mekanism.common.tests.container;

import mekanism.api.AutomationType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tests.helpers.ContainerGameTestHelper;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = "container.rate_limit")
public class ContainerRateLimitTest {

    @GameTest
    @TestHolder(description = "Tests the rate limit for chemical tanks.")
    public static void testChemicalTankLimit(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(1, 1, 1).set(0, 0, 0, MekanismBlocks.BASIC_CHEMICAL_TANK.defaultState()));
        test.onGameTest(ContainerGameTestHelper.class, helper -> helper.startSequence()
              .thenMap(() -> helper.getBlockEntity(0, 0, 0, TileEntityChemicalTank.class).getChemicalTank())
              .thenExecute(tank -> helper.testTransfer(tank, MekanismChemicals.HYDROGEN.asResource(), ChemicalTankTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenExecuteAfter(1, tank -> helper.testTransfer(tank, MekanismChemicals.HYDROGEN.asResource(), ChemicalTankTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenSucceed()
        );
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests the rate limit for item backed chemical tanks.")
    public static void testItemChemicalLimit(final ContainerGameTestHelper helper) {
        helper.startSequence()
              .thenMap(() -> helper.getChemicalTank(MekanismBlocks.BASIC_CHEMICAL_TANK.getItemHolder()))
              .thenExecute(tank -> helper.testTransfer(tank, MekanismChemicals.HYDROGEN.asResource(), ChemicalTankTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenExecuteAfter(1, tank -> helper.testTransfer(tank, MekanismChemicals.HYDROGEN.asResource(), ChemicalTankTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenSucceed();
    }

    @GameTest
    @TestHolder(description = "Tests the rate limit for fluid tanks.")
    public static void testFluidTankLimit(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(1, 1, 1).set(0, 0, 0, MekanismBlocks.BASIC_FLUID_TANK.defaultState()));
        test.onGameTest(ContainerGameTestHelper.class, helper -> helper.startSequence()
              .thenMap(() -> helper.getBlockEntity(0, 0, 0, TileEntityFluidTank.class).fluidTank)
              .thenExecute(tank -> helper.testTransfer(tank, FluidResource.of(Fluids.WATER), FluidTankTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenExecuteAfter(1, tank -> helper.testTransfer(tank, FluidResource.of(Fluids.WATER), FluidTankTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenSucceed()
        );
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests the rate limit for item backed fluid tanks.")
    public static void testItemFluidLimit(final ContainerGameTestHelper helper) {
        helper.startSequence()
              .thenMap(() -> helper.getFluidTank(MekanismBlocks.BASIC_FLUID_TANK.getItemHolder()))
              .thenExecute(tank -> helper.testTransfer(tank, FluidResource.of(Fluids.WATER), FluidTankTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenExecuteAfter(1, tank -> helper.testTransfer(tank, FluidResource.of(Fluids.WATER), FluidTankTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenSucceed();
    }

    @GameTest
    @TestHolder(description = "Tests the rate limit for energy cubes.")
    public static void testEnergyCubeLimit(final DynamicTest test) {
        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(1, 1, 1).set(0, 0, 0, MekanismBlocks.BASIC_ENERGY_CUBE.defaultState()));
        test.onGameTest(ContainerGameTestHelper.class, helper -> helper.startSequence()
              .thenMap(() -> helper.getBlockEntity(0, 0, 0, TileEntityEnergyCube.class).energyContainer())
              .thenExecute(container -> helper.testTransfer(container, EnergyCubeTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenExecuteAfter(1, container -> helper.testTransfer(container, EnergyCubeTier.BASIC.getTransferRate(), AutomationType.INTERNAL, AutomationType.MANUAL))
              .thenSucceed()
        );
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests the rate limit for item backed energy cubes.")
    public static void testItemEnergyLimit(final ContainerGameTestHelper helper) {
        helper.startSequence()
              .thenMap(() -> helper.getEnergyContainer(MekanismBlocks.BASIC_ENERGY_CUBE.getItemHolder()))
              .thenExecute(container -> helper.testTransfer(container, EnergyCubeTier.BASIC.getTransferRate(), AutomationType.EXTERNAL, AutomationType.MANUAL))
              .thenExecuteAfter(1, container -> helper.testTransfer(container, EnergyCubeTier.BASIC.getTransferRate(), AutomationType.EXTERNAL, AutomationType.MANUAL))
              .thenSucceed();
    }
}