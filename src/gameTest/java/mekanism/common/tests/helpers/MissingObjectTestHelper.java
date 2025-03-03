package mekanism.common.tests.helpers;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.EnumColor;
import mekanism.common.attachments.FilterAware;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.attachments.OverflowAware;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.qio.PortableDashboardContents;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

@NothingNullByDefault
public class MissingObjectTestHelper extends MekGameTestHelper {

    private static final Holder<Item> ITEM_TO_REPLACE = MekanismItems.INFUSED_ALLOY;
    private static final Holder<Fluid> FLUID_TO_REPLACE = MekanismFluids.HYDROGEN;
    private static final Holder<Chemical> CHEMICAL_TO_REPLACE = MekanismChemicals.HYDROGEN;
    public static final UnaryOperator<String> REPLACE_TO_INVALID_ITEM = replaceInvalid(ITEM_TO_REPLACE);
    public static final UnaryOperator<String> REPLACE_TO_INVALID_FLUID = replaceInvalid(FLUID_TO_REPLACE);
    public static final UnaryOperator<String> REPLACE_TO_INVALID_CHEMICAL = replaceInvalid(CHEMICAL_TO_REPLACE);

    private static UnaryOperator<String> replaceInvalid(Holder<?> providerToReplace) {
        return rawJson -> rawJson.replaceAll(providerToReplace.getRegisteredName(), "mekanism:invalid");
    }

    public MissingObjectTestHelper(GameTestInfo info) {
        super(info);
    }

    public HashedItem failureHashedItem() {
        return HashedItem.raw(failureItem());
    }

    public ItemStack failureItem() {
        return failureItem(1);
    }

    public ItemStack failureItem(int count) {
        return new ItemStack(ITEM_TO_REPLACE, count);
    }

    public FluidStack failureFluid() {
        return failureFluid(FluidType.BUCKET_VOLUME);
    }

    public FluidStack failureFluid(int amount) {
        return new FluidStack(FLUID_TO_REPLACE, amount);
    }

    public ChemicalStack failureChemical() {
        return failureChemical(FluidType.BUCKET_VOLUME);
    }

    public ChemicalStack failureChemical(long amount) {
        return new ChemicalStack(CHEMICAL_TO_REPLACE, amount);
    }

    public <TYPE> void succeedIfInvalidItemSerializationCycle(Codec<TYPE> codec, Function<MissingObjectTestHelper, TYPE> sourceSupplier, Predicate<TYPE> resultValidator) {
        succeedIfSerializationCycle(codec, sourceSupplier, resultValidator, REPLACE_TO_INVALID_ITEM);
    }

    public <TYPE> void succeedIfInvalidFluidSerializationCycle(Codec<TYPE> codec, Function<MissingObjectTestHelper, TYPE> sourceSupplier, Predicate<TYPE> resultValidator) {
        succeedIfSerializationCycle(codec, sourceSupplier, resultValidator, REPLACE_TO_INVALID_FLUID);
    }

    public <TYPE> void succeedIfInvalidChemicalSerializationCycle(Codec<TYPE> codec, Function<MissingObjectTestHelper, TYPE> sourceSupplier, Predicate<TYPE> resultValidator) {
        succeedIfSerializationCycle(codec, sourceSupplier, resultValidator, REPLACE_TO_INVALID_CHEMICAL);
    }

    public <TYPE> void succeedIfSerializationCycle(Codec<TYPE> codec, Function<MissingObjectTestHelper, TYPE> sourceSupplier, Predicate<TYPE> resultValidator,
          Function<String, String> rawJsonReplacer) {
        succeedIf(() -> {
            TYPE val = cycleSerialization(codec, sourceSupplier.apply(this), rawJsonReplacer);
            if (!resultValidator.test(val)) {//TODO: Allow for custom messages?
                throw new GameTestAssertException("Resulting value after cycling serialization was not what was expected");
            }
        });
    }

    public <TYPE> void succeedIfSerializationCycle(Codec<TYPE> codec, Function<MissingObjectTestHelper, TYPE> sourceSupplier, Function<String, String> rawJsonReplacer) {
        succeedIf(() -> {
            TYPE sourceObject = sourceSupplier.apply(this);
            TYPE val = cycleSerialization(codec, sourceObject, rawJsonReplacer);
            if (!sourceObject.equals(val)) {
                throw new GameTestAssertException("Resulting value after cycling serialization was not what was expected");
            }
        });
    }

    public FormulaAttachment makeFormula() {
        List<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        stacks.set(0, planks.copy());
        stacks.set(2, planks.copy());
        stacks.set(3, planks.copy());
        stacks.set(4, failureItem());
        stacks.set(5, planks.copy());
        return new FormulaAttachment(stacks, false);
    }

    public OverflowAware makeOverflow() {
        Object2IntSortedMap<HashedItem> overflow = new Object2IntLinkedOpenHashMap<>();
        overflow.put(hashedStack(Items.DIAMOND), 10);
        overflow.put(hashedStack(Items.STICK), 4);
        overflow.put(failureHashedItem(), 7);
        overflow.put(hashedStack(Items.STONE), 2);
        return new OverflowAware(overflow);
    }

    public boolean validateOverflow(OverflowAware overflowAware) {
        Object2IntSortedMap<HashedItem> overflow = overflowAware.overflow();
        return overflow.size() == 3 &&
               overflow.getInt(hashedStack(Items.DIAMOND)) == 10 &&
               overflow.getInt(hashedStack(Items.STICK)) == 4 &&
               overflow.getInt(hashedStack(Items.STONE)) == 2;
    }

    public PortableDashboardContents makeDashboard() {
        return PortableDashboardContents.EMPTY
              //First crafting window has a recipe for sticks stored
              .with(0, 1, new ItemStack(Items.OAK_PLANKS, 4))
              .with(0, 4, new ItemStack(Items.OAK_PLANKS, 5))
              //Second has some contents, recipe doesn't matter as one of the things will be invalid
              .with(1, 0, new ItemStack(Items.STONE))
              .with(1, 4, failureItem())
              //Third window has a recipe for planks
              .with(2, 8, new ItemStack(Items.OAK_LOG, 64));
    }

    public boolean validateDashboard(PortableDashboardContents contents) {
        return contents.contents().size() == PortableDashboardContents.TOTAL_SLOTS &&
               //First window
               ItemStack.matches(contents.getSlotContents(0, 1), new ItemStack(Items.OAK_PLANKS, 4)) &&
               ItemStack.matches(contents.getSlotContents(0, 4), new ItemStack(Items.OAK_PLANKS, 5)) &&
               //Second window
               ItemStack.matches(contents.getSlotContents(1, 0), new ItemStack(Items.STONE)) &&
               //Third window
               ItemStack.matches(contents.getSlotContents(2, 8), new ItemStack(Items.OAK_LOG, 64));
    }

    private Map<Upgrade, Integer> getUpgrades() {
        return Map.of(
              Upgrade.SPEED, 5,
              Upgrade.ENERGY, 3
        );
    }

    private UpgradeAware makeUpgrades(boolean validFirstSlot, boolean validSecondSlot) {
        return new UpgradeAware(getUpgrades(),
              validFirstSlot ? MekanismItems.SPEED_UPGRADE.asStack(3) : failureItem(3),
              validSecondSlot ? MekanismItems.ENERGY_UPGRADE.asStack(5) : failureItem(5)
        );
    }

    private boolean validateUpgrades(UpgradeAware upgradeAware, boolean validFirstSlot, boolean validSecondSlot) {
        if (upgradeAware.upgrades().equals(getUpgrades())) {
            boolean firstSlot = validFirstSlot ? ItemStack.matches(MekanismItems.SPEED_UPGRADE.asStack(3), upgradeAware.inputSlot()) : upgradeAware.inputSlot().isEmpty();
            boolean secondSlot = validSecondSlot ? ItemStack.matches(MekanismItems.ENERGY_UPGRADE.asStack(5), upgradeAware.outputSlot()) : upgradeAware.outputSlot().isEmpty();
            return firstSlot && secondSlot;
        }
        return false;
    }

    public void testUpgradeAware(boolean validFirstSlot, boolean validSecondSlot) {
        succeedIfInvalidItemSerializationCycle(UpgradeAware.CODEC, help -> help.makeUpgrades(validFirstSlot, validSecondSlot),
              upgradeAware -> validateUpgrades(upgradeAware, validFirstSlot, validSecondSlot));
    }

    public void testUpgradeAwareOnItem(boolean validFirstSlot, boolean validSecondSlot) {
        succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack smelterItem = new ItemStack(MekanismBlocks.ENERGIZED_SMELTER);
            smelterItem.set(MekanismDataComponents.UPGRADES, help.makeUpgrades(validFirstSlot, validSecondSlot));
            return smelterItem;
        }, smelterItem -> smelterItem.is(MekanismBlocks.ENERGIZED_SMELTER.getItemHolder()) &&
                          validateUpgrades(smelterItem.getOrDefault(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY), validFirstSlot, validSecondSlot));
    }

    public MinerItemStackFilter makeMinerFilter(Item item) {
        MinerItemStackFilter filter = new MinerItemStackFilter();
        filter.setItem(item);
        filter.replaceTarget = Items.COBBLESTONE;
        if (item == Items.STONE) {
            filter.requiresReplacement = true;
        }
        return filter;
    }

    public boolean testFilter(MinerItemStackFilter filter) {
        return filter.replaceTargetMatches(Items.COBBLESTONE) && isStone(filter) == filter.requiresReplacement;
    }

    public SorterItemStackFilter makeSorterFilter(Item item) {
        SorterItemStackFilter filter = new SorterItemStackFilter();
        filter.setItem(item);
        filter.min = 2;
        filter.max  = 3;
        filter.color = EnumColor.AQUA;
        filter.sizeMode = true;
        if (item == Items.STONE) {
            filter.fuzzyMode = true;
        }
        return filter;
    }

    public boolean testFilter(SorterItemStackFilter filter) {
        return filter.min == 2 && filter.max == 3 && filter.color == EnumColor.AQUA && filter.sizeMode && !filter.allowDefault && isStone(filter) == filter.fuzzyMode;
    }

    public QIOItemStackFilter makeQIOFilter(Item item) {
        QIOItemStackFilter filter = new QIOItemStackFilter();
        filter.setItem(item);
        if (item == Items.STONE) {
            filter.fuzzyMode = true;
        }
        return filter;
    }

    private boolean isStone(IItemStackFilter<?> filter) {
        return filter.getItemStack().is(Items.STONE);
    }

    public boolean testFilter(QIOItemStackFilter filter) {
        return isStone(filter) == filter.fuzzyMode;
    }

    private <FILTER extends BaseFilter<FILTER> & IItemStackFilter<FILTER>> FilterAware makeFilters(Function<Item, FILTER> filterCreator) {
        return new FilterAware(List.of(
              filterCreator.apply(Items.STICK),
              filterCreator.apply(ITEM_TO_REPLACE.value()),
              filterCreator.apply(Items.STONE)
        ));
    }

    @SuppressWarnings("unchecked")
    private <FILTER extends BaseFilter<FILTER> & IItemStackFilter<FILTER>> boolean validateFilters(FilterAware filterAware, Predicate<FILTER> filterTester) {
        List<IFilter<?>> filters = filterAware.filters();
        if (filters.size() == 2) {
            FILTER stickFilter = (FILTER) filters.getFirst();
            FILTER stoneFilter = (FILTER) filters.getLast();
            return stickFilter.getItemStack().is(Items.STICK) && filterTester.test(stickFilter) &&
                   isStone(stoneFilter) && filterTester.test(stoneFilter);
        }
        return false;
    }

    public <FILTER extends BaseFilter<FILTER> & IItemStackFilter<FILTER>> void testFilterAware(Function<Item, FILTER> filterCreator, Predicate<FILTER> filterTester) {
        succeedIfInvalidItemSerializationCycle(FilterAware.CODEC, help -> help.makeFilters(filterCreator),
              filterAware -> validateFilters(filterAware, filterTester));
    }

    public <FILTER extends BaseFilter<FILTER> & IItemStackFilter<FILTER>> void testFilterAwareOnItem(Holder<Item> item, Function<Item, FILTER> filterCreator,
          Predicate<FILTER> filterTester) {
        succeedIfInvalidItemSerializationCycle(ItemStack.CODEC, help -> {
            ItemStack stack = new ItemStack(item);
            stack.set(MekanismDataComponents.FILTER_AWARE, help.makeFilters(filterCreator));
            return stack;
        }, stack -> stack.is(item) && validateFilters(stack.getOrDefault(MekanismDataComponents.FILTER_AWARE, FilterAware.EMPTY), filterTester));
    }
}