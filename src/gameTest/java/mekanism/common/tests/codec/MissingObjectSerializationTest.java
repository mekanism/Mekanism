
package mekanism.common.tests.codec;

import java.util.Optional;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.attachments.LockData;
import mekanism.common.attachments.OverflowAware;
import mekanism.common.attachments.containers.chemical.AttachedChemicals;
import mekanism.common.attachments.containers.fluid.AttachedFluids;
import mekanism.common.attachments.containers.item.AttachedItems;
import mekanism.common.attachments.qio.PortableDashboardContents;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tests.helpers.MissingObjectTestHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "codec.missing")
public class MissingObjectSerializationTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that the lenient optional stack codec returns empty instead of throwing if used to deserialize an invalid stack.")
    public static void testLenientOptionalStack(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(SerializerHelper.LENIENT_OPTIONAL_STACK_CODEC, MissingObjectTestHelper::failureItem, ItemStack::isEmpty);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that the lenient optional fluid stack codec returns empty instead of throwing if used to deserialize an invalid stack.")
    public static void testLenientOptionalFluidStack(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidFluidSerializationCycle(SerializerHelper.LENIENT_OPTIONAL_FLUID_CODEC, MissingObjectTestHelper::failureFluid, FluidStack::isEmpty);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that the lenient optional chemical stack codec returns empty instead of throwing if used to deserialize an invalid stack.")
    public static void testLenientOptionalChemicalStack(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidChemicalSerializationCycle(ChemicalStack.LENIENT_OPTIONAL_CODEC, MissingObjectTestHelper::failureChemical, ChemicalStack::isEmpty);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that attached items load as best as they can when an item is missing.")
    public static void testAttachedItems(final MissingObjectTestHelper helper) {
        ItemStack initialStick = new ItemStack(Items.STICK, 10);
        ItemStack initialStone = new ItemStack(Items.STONE, 5);
        helper.succeedIfInvalidItemSerializationCycle(AttachedItems.CODEC, help -> new AttachedItems(NonNullList.of(ItemStack.EMPTY,
              initialStick.copy(),
              help.failureItem(3),
              initialStone.copy()
        )), attached -> attached.size() == 3 &&
                        ItemStack.matches(attached.get(0), initialStick) &&
                        attached.get(1).isEmpty() &&
                        ItemStack.matches(attached.get(2), initialStone));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that attached fluids load as best as they can when a fluid is missing.")
    public static void testAttachedFluids(final MissingObjectTestHelper helper) {
        FluidStack initialWater = new FluidStack(Fluids.WATER, 10);
        FluidStack initialLava = new FluidStack(Fluids.LAVA, 5);
        helper.succeedIfInvalidFluidSerializationCycle(AttachedFluids.CODEC, help -> new AttachedFluids(NonNullList.of(FluidStack.EMPTY,
              initialWater.copy(),
              help.failureFluid(3),
              initialLava.copy()
        )), attached -> attached.size() == 3 &&
                        FluidStack.matches(attached.get(0), initialWater) &&
                        attached.get(1).isEmpty() &&
                        FluidStack.matches(attached.get(2), initialLava));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that attached chemicals load as best as they can when a chemical is missing.")
    public static void testAttachedChemicals(final MissingObjectTestHelper helper) {
        ChemicalStack initialAntimatter = MekanismChemicals.ANTIMATTER.asStack(10);
        ChemicalStack initialGold = MekanismChemicals.GOLD.asStack(5);
        helper.succeedIfInvalidChemicalSerializationCycle(AttachedChemicals.CODEC, help -> new AttachedChemicals(NonNullList.of(ChemicalStack.EMPTY,
              initialAntimatter.copy(),
              help.failureChemical(3),
              initialGold.copy()
        )), attached -> attached.size() == 3 &&
                        attached.get(0).equals(initialAntimatter) &&
                        attached.get(1).isEmpty() &&
                        attached.get(2).equals(initialGold));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that formulas that contain invalid items fall back to an empty formula.")
    public static void testFormulaAttachment(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(FormulaAttachment.CODEC, MissingObjectTestHelper::makeFormula, FormulaAttachment::isEmpty);
    }

    @GameTest
    @EmptyTemplate
    @SuppressWarnings("OptionalAssignedToNull")
    @TestHolder(description = "Tests to make sure that formula items that have formulas that contain invalid items will load as having no formula.")
    public static void testFormulaAttachmentOnItem(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack formulaItem = MekanismItems.CRAFTING_FORMULA.asStack();
            formulaItem.set(MekanismDataComponents.FORMULA_HOLDER, help.makeFormula());
            return formulaItem;
        }, formulaItem -> {
            if (formulaItem.is(MekanismItems.CRAFTING_FORMULA)) {
                FormulaAttachment formula = formulaItem.get(MekanismDataComponents.FORMULA_HOLDER);
                return formula != null && formula.isEmpty() && formulaItem.getComponentsPatch().get(MekanismDataComponents.FORMULA_HOLDER.get()) == null;
            }
            return false;
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that lock data that contain invalid items fall back to no lock data.")
    public static void testLockData(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(LockData.CODEC, help -> LockData.create(help.failureItem()), LockData.EMPTY::equals);
    }

    @GameTest
    @EmptyTemplate
    @SuppressWarnings("OptionalAssignedToNull")
    @TestHolder(description = "Tests to make sure that bins that are locked to an invalid item will load as not being locked.")
    public static void testLockDataOnItem(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack binItem = new ItemStack(MekanismBlocks.BASIC_BIN);
            binItem.set(MekanismDataComponents.LOCK, LockData.create(help.failureItem()));
            return binItem;
        }, binItem -> binItem.is(MekanismBlocks.BASIC_BIN.getItemHolder()) && LockData.EMPTY.equals(binItem.get(MekanismDataComponents.LOCK)) &&
                      binItem.getComponentsPatch().get(MekanismDataComponents.LOCK.get()) == null);
    }

    @GameTest
    @EmptyTemplate
    @SuppressWarnings("OptionalAssignedToNull")
    @TestHolder(description = "Tests to make sure that redstone adapters with a target that are targeting an invalid item, will load sa if they have no target.")
    public static void testItemTarget(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack adapter = new ItemStack(MekanismBlocks.QIO_REDSTONE_ADAPTER);
            adapter.set(MekanismDataComponents.ITEM_TARGET, Optional.of(help.failureHashedItem()));
            adapter.set(MekanismDataComponents.LONG_AMOUNT, 5L);
            return adapter;
        }, adapter -> {
            if (adapter.is(MekanismBlocks.QIO_REDSTONE_ADAPTER.getItemHolder())) {
                Optional<HashedItem> itemTarget = adapter.get(MekanismDataComponents.ITEM_TARGET);
                return itemTarget != null && itemTarget.isEmpty() && adapter.getComponentsPatch().get(MekanismDataComponents.ITEM_TARGET.get()) == null &&
                       adapter.getOrDefault(MekanismDataComponents.LONG_AMOUNT, 0L) == 5;
            }
            return false;
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that overflow that contain invalid items, keeps all still valid items, and ignores the invalid ones.")
    public static void testOverflowAware(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(OverflowAware.CODEC, MissingObjectTestHelper::makeOverflow, helper::validateOverflow);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that overflow that contain invalid items, will load all still valid items and ignore the invalid ones.")
    public static void testOverflowAwareOnItem(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack minerItem = new ItemStack(MekanismBlocks.DIGITAL_MINER);
            minerItem.set(MekanismDataComponents.OVERFLOW_AWARE, help.makeOverflow());
            return minerItem;
        }, minerItem -> minerItem.is(MekanismBlocks.DIGITAL_MINER.getItemHolder()) &&
                        helper.validateOverflow(minerItem.getOrDefault(MekanismDataComponents.OVERFLOW_AWARE, OverflowAware.EMPTY)));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that portable dashboard contents that contain invalid items, keeps all still valid items, and ignores the invalid ones.")
    public static void testDashboardContents(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(PortableDashboardContents.CODEC, MissingObjectTestHelper::makeDashboard, helper::validateDashboard);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that portable dashboards that contain invalid items, will load all still valid items and ignore the invalid ones.")
    public static void testDashboardContentsOnItem(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack dashboardItem = MekanismItems.PORTABLE_QIO_DASHBOARD.asStack();
            dashboardItem.set(MekanismDataComponents.QIO_DASHBOARD, help.makeDashboard());
            return dashboardItem;
        }, dashboardItem -> dashboardItem.is(MekanismItems.PORTABLE_QIO_DASHBOARD) &&
                            helper.validateDashboard(dashboardItem.getOrDefault(MekanismDataComponents.QIO_DASHBOARD, PortableDashboardContents.EMPTY)));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that upgrade components that contain invalid items, ignore the invalid ones.")
    public static void testUpgradeAware(final MissingObjectTestHelper helper) {
        helper.testUpgradeAware(false, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that items with upgrade components that contain invalid items, ignore the invalid ones.")
    public static void testUpgradeAwareOnItem(final MissingObjectTestHelper helper) {
        helper.testUpgradeAwareOnItem(false, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that upgrade components that contain an invalid second item, ignores it.")
    public static void testUpgradeAwareFirstValid(final MissingObjectTestHelper helper) {
        helper.testUpgradeAware(true, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that items with upgrade components that contain an invalid second item, ignores it.")
    public static void testUpgradeAwareOnItemFirstValid(final MissingObjectTestHelper helper) {
        helper.testUpgradeAwareOnItem(true, false);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that upgrade components that contain an invalid first item, ignores it.")
    public static void testUpgradeAwareSecondValid(final MissingObjectTestHelper helper) {
        helper.testUpgradeAware(false, true);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that items with upgrade components that contain an invalid first item, ignores it.")
    public static void testUpgradeAwareOnItemSecondValid(final MissingObjectTestHelper helper) {
        helper.testUpgradeAwareOnItem(false, true);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that when a miner filter is invalid, it gets properly skipped.")
    public static void testMinerFilterAware(final MissingObjectTestHelper helper) {
        helper.testFilterAware(helper::makeMinerFilter, helper::testFilter);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that when an item storing miner filters has an invalid filter, it gets properly skipped.")
    public static void testMinerFilterAwareOnItem(final MissingObjectTestHelper helper) {
        helper.testFilterAwareOnItem(MekanismBlocks.DIGITAL_MINER.getItemHolder(), helper::makeMinerFilter, helper::testFilter);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that when a sorter filter is invalid, it gets properly skipped.")
    public static void testSorterFilterAware(final MissingObjectTestHelper helper) {
        helper.testFilterAware(helper::makeSorterFilter, helper::testFilter);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that when an item storing sorter filters has an invalid filter, it gets properly skipped.")
    public static void testSorterFilterAwareOnItem(final MissingObjectTestHelper helper) {
        helper.testFilterAwareOnItem(MekanismBlocks.LOGISTICAL_SORTER.getItemHolder(), helper::makeSorterFilter, helper::testFilter);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that when a QIO filter is invalid, it gets properly skipped.")
    public static void testQIOFilterAware(final MissingObjectTestHelper helper) {
        helper.testFilterAware(helper::makeQIOFilter, helper::testFilter);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that when an item storing QIO filters has an invalid filter, it gets properly skipped.")
    public static void testQIOFilterAwareOnItem(final MissingObjectTestHelper helper) {
        helper.testFilterAwareOnItem(MekanismBlocks.QIO_IMPORTER.getItemHolder(), helper::makeQIOFilter, helper::testFilter);
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that invalid chemicals, fluids, and items are skipped when loading inventory frequencies, without breaking the rest of the stored data.")
    public static void testInventoryFrequency(final MissingObjectTestHelper helper) {
        helper.succeedIfSerializationCycle(InventoryFrequency.CODEC, help -> {
                  InventoryFrequency frequency = new InventoryFrequency("test", null, SecurityMode.PUBLIC);
                  frequency.getEnergyContainers(null).getFirst().setEnergy(100);
                  frequency.getHeatCapacitors(null).getFirst().setHeat(1_000);
                  frequency.getChemicalTanks(null).getFirst().setStack(help.failureChemical());
                  frequency.getFluidTanks(null).getFirst().setStack(help.failureFluid());
                  frequency.getInventorySlots(null).getFirst().setStack(help.failureItem());
                  return frequency;
              }, frequency -> frequency.getName().equals("test") && frequency.getSecurity() == SecurityMode.PUBLIC &&
                              frequency.getEnergyContainers(null).getFirst().getEnergy() == 100 &&
                              frequency.getHeatCapacitors(null).getFirst().getHeat() == 1_000 &&
                              frequency.getChemicalTanks(null).getFirst().isEmpty() &&
                              frequency.getFluidTanks(null).getFirst().isEmpty() &&
                              frequency.getInventorySlots(null).getFirst().isEmpty(),
              MissingObjectTestHelper.REPLACE_TO_INVALID_ITEM
                    .andThen(MissingObjectTestHelper.REPLACE_TO_INVALID_FLUID)
                    .andThen(MissingObjectTestHelper.REPLACE_TO_INVALID_CHEMICAL)
        );
    }
}