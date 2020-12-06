package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackToItemStackCachedRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.upgrade.MachineUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

//Smelting, enriching, crushing
public class TileEntityItemStackToItemStackFactory extends TileEntityItemToItemFactory<ItemStackToItemStackRecipe> {

    public TileEntityItemStackToItemStackFactory(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return containsRecipe(recipe -> recipe.getInput().testType(stack));
    }

    @Override
    protected int getNeededInput(ItemStackToItemStackRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<ItemStackToItemStackRecipe> cached, @Nonnull ItemStack stack) {
        return cached != null && cached.getRecipe().getInput().testType(stack);
    }

    @Override
    protected ItemStackToItemStackRecipe findRecipe(int process, @Nonnull ItemStack fallbackInput, @Nonnull IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot) {
        ItemStack output = outputSlot.getStack();
        return findFirstRecipe(recipe -> recipe.getInput().testType(fallbackInput) && InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput), output));
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackToItemStackRecipe> getRecipeType() {
        switch (type) {
            case ENRICHING:
                return MekanismRecipeType.ENRICHING;
            case CRUSHING:
                return MekanismRecipeType.CRUSHING;
            case SMELTING:
            default:
                //TODO: Make it so that it throws an error if it is not one of the three types
                return MekanismRecipeType.SMELTING;
        }
    }

    @Nullable
    @Override
    public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandlers[cacheIndex].getInput();
        if (stack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack));
    }

    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@Nonnull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        return new ItemStackToItemStackCachedRecipe(recipe, inputHandlers[cacheIndex], outputHandlers[cacheIndex])
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @Nonnull
    @Override
    public MachineUpgradeData getUpgradeData() {
        return new MachineUpgradeData(redstone, getControlType(), getEnergyContainer(), progress, energySlot, inputSlots, outputSlots, isSorting(), getComponents());
    }
}