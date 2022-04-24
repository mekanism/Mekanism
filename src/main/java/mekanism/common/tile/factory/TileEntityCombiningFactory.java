package mekanism.common.tile.factory;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.DoubleItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.upgrade.CombinerUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCombiningFactory extends TileEntityItemToItemFactory<CombinerRecipe> implements DoubleItemRecipeLookupHandler<CombinerRecipe> {

    private static final Map<RecipeError, Boolean> TRACKED_ERROR_TYPES = Map.of(
          RecipeError.NOT_ENOUGH_ENERGY, true,
          RecipeError.NOT_ENOUGH_INPUT, false,
          RecipeError.NOT_ENOUGH_SECONDARY_INPUT, true,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE, false,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT, false
    );

    private final IInputHandler<@NonNull ItemStack> extraInputHandler;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getSecondaryInput")
    private InputInventorySlot extraSlot;

    public TileEntityCombiningFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES);
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
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return containsRecipeA(stack);
    }

    @Override
    protected int getNeededInput(CombinerRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getMainInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<CombinerRecipe> cached, @Nonnull ItemStack stack) {
        if (cached != null) {
            CombinerRecipe cachedRecipe = cached.getRecipe();
            return cachedRecipe.getMainInput().testType(stack) && (extraSlot.isEmpty() || cachedRecipe.getExtraInput().testType(extraSlot.getStack()));
        }
        return false;
    }

    @Override
    protected CombinerRecipe findRecipe(int process, @Nonnull ItemStack fallbackInput, @Nonnull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot) {
        ItemStack extra = extraSlot.getStack();
        ItemStack output = outputSlot.getStack();
        //TODO: Give it something that is not empty when we don't have a stored secondary stack for getting the output?
        return getRecipeType().getInputCache().findTypeBasedRecipe(level, fallbackInput, extra,
              recipe -> InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput, extra), output));
    }

    @Nonnull
    @Override
    public IMekanismRecipeTypeProvider<CombinerRecipe, DoubleItem<CombinerRecipe>> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }

    @Nullable
    @Override
    public CombinerRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandlers[cacheIndex], extraInputHandler);
    }

    @Nonnull
    @Override
    public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@Nonnull CombinerRecipe recipe, int cacheIndex) {
        return TwoInputCachedRecipe.combiner(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], extraInputHandler, outputHandlers[cacheIndex])
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof CombinerUpgradeData data) {
            //Generic factory upgrade data handling
            super.parseUpgradeData(upgradeData);
            //Copy the stack using NBT so that if it is not actually valid due to a reload we don't crash
            extraSlot.deserializeNBT(data.extraSlot.serializeNBT());
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
        }
    }

    @Nonnull
    @Override
    public CombinerUpgradeData getUpgradeData() {
        return new CombinerUpgradeData(redstone, getControlType(), getEnergyContainer(), progress, energySlot, extraSlot, inputSlots, outputSlots, isSorting(), getComponents());
    }
}