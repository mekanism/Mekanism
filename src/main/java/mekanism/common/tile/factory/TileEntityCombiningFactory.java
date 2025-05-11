package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.DoubleItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.DoubleInputRecipeCache.CheckRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.upgrade.CombinerUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityCombiningFactory extends TileEntityItemToItemFactory<CombinerRecipe> implements DoubleItemRecipeLookupHandler<CombinerRecipe> {

    private static final CheckRecipeType<ItemStack, ItemStack, CombinerRecipe, ItemStack> OUTPUT_CHECK =
          (recipe, input, extra, output) -> InventoryUtils.areItemsStackable(recipe.getOutput(input, extra), output);
    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    private static final Set<RecipeError> GLOBAL_ERROR_TYPES = Set.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_SECONDARY_INPUT
    );

    private final IInputHandler<@NotNull ItemStack> extraInputHandler;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getSecondaryInput", docPlaceholder = "secondary input slot")
    InputInventorySlot extraSlot;

    public TileEntityCombiningFactory(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
        extraInputHandler = InputHelper.getInputHandler(extraSlot, RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
    }

    @Override
    protected void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener) {
        super.addSlots(builder, listener, updateSortingListener);
        builder.addSlot(extraSlot = InputInventorySlot.at(this::containsRecipeB, markAllMonitorsChanged(listener), 7, 57));
        extraSlot.setSlotType(ContainerSlotType.EXTRA);
    }

    @Nullable
    @Override
    protected InputInventorySlot getExtraSlot() {
        return extraSlot;
    }

    @Override
    public boolean isItemValidForSlot(@NotNull ItemStack stack) {
        return containsRecipeAB(stack, extraSlot.getStack());
    }

    @Override
    public boolean isValidInputItem(@NotNull ItemStack stack) {
        return containsRecipeA(stack);
    }

    @Override
    protected int getNeededInput(CombinerRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getMainInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<CombinerRecipe> cached, @NotNull ItemStack stack) {
        if (cached != null) {
            CombinerRecipe cachedRecipe = cached.getRecipe();
            return cachedRecipe.getMainInput().testType(stack) && (extraSlot.isEmpty() || cachedRecipe.getExtraInput().testType(extraSlot.getStack()));
        }
        return false;
    }

    @Override
    protected CombinerRecipe findRecipe(int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot) {
        //TODO: Give it something that is not empty when we don't have a stored secondary stack for getting the output?
        return getRecipeType().getInputCache().findTypeBasedRecipe(level, fallbackInput, extraSlot.getStack(), outputSlot.getStack(), OUTPUT_CHECK);
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<RecipeInput, CombinerRecipe, DoubleItem<CombinerRecipe>> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }

    @Override
    public IRecipeViewerRecipeType<CombinerRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.COMBINING;
    }

    @Nullable
    @Override
    public CombinerRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandlers[cacheIndex], extraInputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@NotNull CombinerRecipe recipe, int cacheIndex) {
        return TwoInputCachedRecipe.combiner(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], extraInputHandler, outputHandlers[cacheIndex])
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(this::canFunction)
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks)
              .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    @Override
    public void parseUpgradeData(HolderLookup.Provider provider, @NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof CombinerUpgradeData data) {
            //Generic factory upgrade data handling
            super.parseUpgradeData(provider, upgradeData);
            //Copy the stack using NBT so that if it is not actually valid due to a reload we don't crash
            extraSlot.deserializeNBT(provider, data.extraSlot.serializeNBT(provider));
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
        }
    }

    @NotNull
    @Override
    public CombinerUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new CombinerUpgradeData(provider, redstone, getControlType(), getEnergyContainer(), progress, energySlot, extraSlot, inputSlots, outputSlots, isSorting(), getComponents());
    }
}
