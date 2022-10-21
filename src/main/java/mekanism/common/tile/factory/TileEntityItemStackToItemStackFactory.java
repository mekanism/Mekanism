package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.upgrade.MachineUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Smelting, enriching, crushing
public class TileEntityItemStackToItemStackFactory extends TileEntityItemToItemFactory<ItemStackToItemStackRecipe> implements
      ItemRecipeLookupHandler<ItemStackToItemStackRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    private static final Set<RecipeError> GLOBAL_ERROR_TYPES = Set.of(RecipeError.NOT_ENOUGH_ENERGY);

    public TileEntityItemStackToItemStackFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
    }

    @Override
    public boolean isValidInputItem(@NotNull ItemStack stack) {
        return containsRecipe(stack);
    }

    @Override
    protected int getNeededInput(ItemStackToItemStackRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<ItemStackToItemStackRecipe> cached, @NotNull ItemStack stack) {
        return cached != null && cached.getRecipe().getInput().testType(stack);
    }

    @Override
    protected ItemStackToItemStackRecipe findRecipe(int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot) {
        ItemStack output = outputSlot.getStack();
        return getRecipeType().getInputCache().findTypeBasedRecipe(level, fallbackInput,
              recipe -> InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput), output));
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return switch (type) {
            case ENRICHING -> MekanismRecipeType.ENRICHING;
            case CRUSHING -> MekanismRecipeType.CRUSHING;
            //TODO: Make it so that it throws an error if it is not one of the three types
            default -> MekanismRecipeType.SMELTING;
        };
    }

    @Nullable
    @Override
    public ItemStackToItemStackRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandlers[cacheIndex]);
    }

    @NotNull
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.itemToItem(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], outputHandlers[cacheIndex])
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @NotNull
    @Override
    public MachineUpgradeData getUpgradeData() {
        return new MachineUpgradeData(redstone, getControlType(), getEnergyContainer(), progress, energySlot, inputSlots, outputSlots, isSorting(), getComponents());
    }
}