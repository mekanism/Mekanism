
package mekanism.common.tests.codec;

import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.container.LargeResourceStack;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.attachments.LockData;
import mekanism.common.attachments.OverflowAware;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.qio.PortableDashboardContents;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tests.helpers.MissingObjectTestHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
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
        //TODO - 26.1: Can we add a helper to generify this test easier now that it is more generic between the resource types?
        LargeResourceStack<ItemResource> initialStick = new LargeResourceStack<>(ItemResource.of(Items.STICK), 10);
        LargeResourceStack<ItemResource> initialStone = new LargeResourceStack<>(ItemResource.of(Items.STONE), 5);
        helper.succeedIfInvalidItemSerializationCycle(MekanismDataComponents.ATTACHED_ITEMS.get().codecOrThrow(), help -> new AttachedResources<>(
              NonNullList.of(LargeResourceStack.EMPTY_ITEM_STACK,
                    initialStick,
                    new LargeResourceStack<>(help.failureItemType(), 3),
                    initialStone
              )), attached -> attached.size() == 3 &&
                              attached.get(0).equals(initialStick) &&
                              attached.get(1).isEmpty() &&
                              attached.get(2).equals(initialStone)
        );
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that attached fluids load as best as they can when a fluid is missing.")
    public static void testAttachedFluids(final MissingObjectTestHelper helper) {
        LargeResourceStack<FluidResource> initialWater = new LargeResourceStack<>(FluidResource.of(Fluids.WATER), 10);
        LargeResourceStack<FluidResource> initialLava = new LargeResourceStack<>(FluidResource.of(Fluids.LAVA), 5);
        helper.succeedIfInvalidFluidSerializationCycle(MekanismDataComponents.ATTACHED_FLUIDS.get().codecOrThrow(), help -> new AttachedResources<>(
              NonNullList.of(LargeResourceStack.EMPTY_FLUID_STACK,
                    initialWater,
                    new LargeResourceStack<>(help.failureFluidType(), 3),
                    initialLava
              )), attached -> attached.size() == 3 &&
                              attached.get(0).equals(initialWater) &&
                              attached.get(1).isEmpty() &&
                              attached.get(2).equals(initialLava)
        );
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that attached chemicals load as best as they can when a chemical is missing.")
    public static void testAttachedChemicals(final MissingObjectTestHelper helper) {
        LargeResourceStack<ChemicalResource> initialAntimatter = new LargeResourceStack<>(MekanismChemicals.ANTIMATTER.asResource(), 10);
        LargeResourceStack<ChemicalResource> initialGold = new LargeResourceStack<>(MekanismChemicals.GOLD.asResource(), 5);
        helper.succeedIfInvalidChemicalSerializationCycle(MekanismDataComponents.ATTACHED_CHEMICALS.get().codecOrThrow(), help -> new AttachedResources<>(
              NonNullList.of(LargeResourceStack.EMPTY_CHEMICAL_STACK,
                    initialAntimatter,
                    new LargeResourceStack<>(help.failureChemicalType(), 3),
                    initialGold
              )), attached -> attached.size() == 3 &&
                              attached.get(0).equals(initialAntimatter) &&
                              attached.get(1).isEmpty() &&
                              attached.get(2).equals(initialGold)
        );
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
                  frequency.getEnergyContainers().getFirst().setEnergy(100);
                  frequency.getHeatCapacitors(null).getFirst().setHeat(1_000);
                  frequency.getChemicalTanks().getFirst().setContents(help.failureChemicalType(), 1);
                  frequency.getFluidTanks().getFirst().setContents(help.failureFluidType(), FluidType.BUCKET_VOLUME);
                  frequency.getInventorySlots().getFirst().setContents(help.failureItemType(), 1);
                  return frequency;
              }, frequency -> frequency.getName().equals("test") && frequency.getSecurity() == SecurityMode.PUBLIC &&
                              frequency.getEnergyContainers().getFirst().getEnergy() == 100 &&
                              frequency.getHeatCapacitors(null).getFirst().getHeat() == 1_000 &&
                              frequency.getChemicalTanks().getFirst().isEmpty() &&
                              frequency.getFluidTanks().getFirst().isEmpty() &&
                              frequency.getInventorySlots().getFirst().isEmpty(),
              MissingObjectTestHelper.REPLACE_TO_INVALID_ITEM
                    .andThen(MissingObjectTestHelper.REPLACE_TO_INVALID_FLUID)
                    .andThen(MissingObjectTestHelper.REPLACE_TO_INVALID_CHEMICAL)
        );
    }
}