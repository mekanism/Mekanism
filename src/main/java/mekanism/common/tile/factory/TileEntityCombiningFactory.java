package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CombinerCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.upgrade.CombinerUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

public class TileEntityCombiningFactory extends TileEntityItemToItemFactory<CombinerRecipe> {

    private final IInputHandler<@NonNull ItemStack> extraInputHandler;

    private InputInventorySlot extraSlot;

    public TileEntityCombiningFactory(IBlockProvider blockProvider) {
        super(blockProvider);
        extraInputHandler = InputHelper.getInputHandler(extraSlot);
    }

    @Override
    protected void addSlots(InventorySlotHelper builder, IContentsListener updateSortingListener) {
        super.addSlots(builder, updateSortingListener);
        builder.addSlot(extraSlot = InputInventorySlot.at(stack -> containsRecipe(recipe -> recipe.getExtraInput().testType(stack)), updateSortingListener, 7, 57));
        extraSlot.setSlotType(ContainerSlotType.EXTRA);
    }

    @Nullable
    @Override
    protected InputInventorySlot getExtraSlot() {
        return extraSlot;
    }

    @Override
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return containsRecipe(recipe -> recipe.getMainInput().testType(stack));
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
        return findFirstRecipe(recipe -> {
            if (recipe.getMainInput().testType(fallbackInput)) {
                if (extra.isEmpty() || recipe.getExtraInput().testType(extra)) {
                    return InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput, extra), output);
                }
            }
            return false;
        });
    }

    @Nonnull
    @Override
    public MekanismRecipeType<CombinerRecipe> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }

    @Nullable
    @Override
    public CombinerRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandlers[cacheIndex].getInput();
        if (stack.isEmpty()) {
            return null;
        }
        ItemStack extra = extraInputHandler.getInput();
        if (extra.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack, extra));
    }

    @Override
    public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@Nonnull CombinerRecipe recipe, int cacheIndex) {
        return new CombinerCachedRecipe(recipe, inputHandlers[cacheIndex], extraInputHandler, outputHandlers[cacheIndex])
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof CombinerUpgradeData) {
            //Generic factory upgrade data handling
            super.parseUpgradeData(upgradeData);
            CombinerUpgradeData data = (CombinerUpgradeData) upgradeData;
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