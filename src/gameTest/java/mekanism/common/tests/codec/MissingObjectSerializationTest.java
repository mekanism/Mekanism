
package mekanism.common.tests.codec;

import mekanism.api.resource.LargeResourceStack;
import mekanism.api.security.SecurityMode;
import mekanism.common.component.FormulaComponent;
import mekanism.common.component.LockData;
import mekanism.common.component.OverflowAware;
import mekanism.common.component.qio.PortableDashboardContents;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tests.helpers.MissingObjectTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "codec.missing")
public class MissingObjectSerializationTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that attached items load as best as they can when an item is missing.")
    public static void testAttachedItems(final MissingObjectTestHelper helper) {
        helper.succeedIfAttachedCycle(MekanismDataComponents.ATTACHED_ITEMS.get(), LargeResourceStack.ITEM_HELPER, helper.failureItemType(),
              ItemResource.of(Items.STICK), ItemResource.of(Items.STONE));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that attached fluids load as best as they can when a fluid is missing.")
    public static void testAttachedFluids(final MissingObjectTestHelper helper) {
        helper.succeedIfAttachedCycle(MekanismDataComponents.ATTACHED_FLUIDS.get(), LargeResourceStack.FLUID_HELPER, helper.failureFluidType(),
              FluidResource.of(Fluids.WATER), FluidResource.of(Fluids.LAVA));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that attached chemicals load as best as they can when a chemical is missing.")
    public static void testAttachedChemicals(final MissingObjectTestHelper helper) {
        helper.succeedIfAttachedCycle(MekanismDataComponents.ATTACHED_CHEMICALS.get(), LargeResourceStack.CHEMICAL_HELPER, helper.failureChemicalType(),
              MekanismChemicals.ANTIMATTER.asResource(), MekanismChemicals.GOLD.asResource());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that formulas that contain invalid items fall back to an empty formula.")
    public static void testFormulaAttachment(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(FormulaComponent.CODEC, MissingObjectTestHelper::makeFormula, FormulaComponent::isEmpty);
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
                FormulaComponent formula = formulaItem.get(MekanismDataComponents.FORMULA_HOLDER);
                return formula != null && formula.isEmpty() && formulaItem.getComponentsPatch().getPatch(MekanismDataComponents.FORMULA_HOLDER.get()) == null;
            }
            return false;
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that lock data that contain invalid items fall back to no lock data.")
    public static void testLockData(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(LockData.CODEC, help -> LockData.create(help.failureItemType()), LockData.EMPTY::equals);
    }

    @GameTest
    @EmptyTemplate
    @SuppressWarnings("OptionalAssignedToNull")
    @TestHolder(description = "Tests to make sure that bins that are locked to an invalid item will load as not being locked.")
    public static void testLockDataOnItem(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack binItem = new ItemStack(MekanismBlocks.BASIC_BIN);
            binItem.set(MekanismDataComponents.LOCK, LockData.create(help.failureItemType()));
            return binItem;
        }, binItem -> binItem.is(MekanismBlocks.BASIC_BIN.getItemHolder()) && LockData.EMPTY.equals(binItem.get(MekanismDataComponents.LOCK)) &&
                      binItem.getComponentsPatch().getPatch(MekanismDataComponents.LOCK.get()) == null);
    }

    @GameTest
    @EmptyTemplate
    @SuppressWarnings("OptionalAssignedToNull")
    @TestHolder(description = "Tests to make sure that redstone adapters with a target that are targeting an invalid item, will load sa if they have no target.")
    public static void testItemTarget(final MissingObjectTestHelper helper) {
        helper.succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack adapter = new ItemStack(MekanismBlocks.QIO_REDSTONE_ADAPTER);
            adapter.set(MekanismDataComponents.ITEM_TARGET, help.failureItemType());
            adapter.set(MekanismDataComponents.LONG_AMOUNT, 5L);
            return adapter;
        }, adapter -> {
            if (adapter.is(MekanismBlocks.QIO_REDSTONE_ADAPTER.getItemHolder())) {
                ItemResource itemTarget = adapter.get(MekanismDataComponents.ITEM_TARGET);
                return itemTarget != null && itemTarget.isEmpty() && adapter.getComponentsPatch().getPatch(MekanismDataComponents.ITEM_TARGET.get()) == null &&
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
                  frequency.getEnergyContainer().setEnergy(100, null);
                  frequency.getHeatCapacitor().setHeat(1_000);
                  frequency.getChemicalTanks().getFirst().setContents(help.failureChemicalType(), 1, null);
                  frequency.getFluidTanks().getFirst().setContents(help.failureFluidType(), FluidType.BUCKET_VOLUME, null);
                  frequency.getInventorySlots().getFirst().setContents(help.failureItemType(), 1, null);
                  return frequency;
              }, frequency -> frequency.getName().equals("test") && frequency.getSecurity() == SecurityMode.PUBLIC &&
                              frequency.getEnergyContainer().getAmountAsLong() == 100 &&
                              frequency.getHeatCapacitor().getHeat() == 1_000 &&
                              frequency.getChemicalTanks().getFirst().isEmpty() &&
                              frequency.getFluidTanks().getFirst().isEmpty() &&
                              frequency.getInventorySlots().getFirst().isEmpty(),
              MissingObjectTestHelper.REPLACE_TO_INVALID_ITEM
                    .andThen(MissingObjectTestHelper.REPLACE_TO_INVALID_FLUID)
                    .andThen(MissingObjectTestHelper.REPLACE_TO_INVALID_CHEMICAL)
        );
    }
}