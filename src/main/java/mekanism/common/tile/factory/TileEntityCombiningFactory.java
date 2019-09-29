package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CombinerCachedRecipe;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityCombiningFactory extends TileEntityFactory<CombinerRecipe> {

    public TileEntityCombiningFactory(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return containsRecipe(recipe -> recipe.getMainInput().testType(stack));
    }

    @Override
    public boolean isValidExtraItem(@Nonnull ItemStack stack) {
        return containsRecipe(recipe -> recipe.getExtraInput().testType(stack));
    }

    @Override
    public boolean inputProducesOutput(int slotID, ItemStack fallbackInput, ItemStack output, boolean updateCache) {
        if (output.isEmpty()) {
            return true;
        }
        int process = getOperation(slotID);
        CachedRecipe<CombinerRecipe> cached = getCachedRecipe(process);
        if (cached != null) {
            CombinerRecipe cachedRecipe = cached.getRecipe();
            ItemStack extra = inventory.get(EXTRA_SLOT_ID);
            if (cachedRecipe.getMainInput().testType(fallbackInput) && (extra.isEmpty() || cachedRecipe.getExtraInput().testType(extra))) {
                //Our input matches the recipe we have cached for this slot
                return true;
            }
            //If there is no cached item input or it doesn't match our fallback then it is an out of date cache, so we ignore the fact that we have a cache
        }
        //TODO: Decide if recipe.getOutput *should* assume that it is given a valid input or not
        // Here we are using it as if it is not assuming it, but that is in part because it currently does not care about the value passed
        // and if something does have extra checking to check the input as long as it checks for invalid ones this should still work
        ItemStack extra = inventory.get(EXTRA_SLOT_ID);
        CombinerRecipe foundRecipe = findFirstRecipe(recipe -> {
            if (recipe.getMainInput().testType(fallbackInput)) {
                if (extra.isEmpty() || recipe.getExtraInput().testType(extra)) {
                    return ItemHandlerHelper.canItemStacksStack(recipe.getOutput(fallbackInput, extra), output);
                }
            }
            return false;
        });
        if (foundRecipe == null) {
            //We could not find any valid recipe for the given item that matches the items in the current output slots
            return false;
        }
        if (updateCache) {
            //If we want to update the cache, then create a new cache with the recipe we found
            CachedRecipe<CombinerRecipe> newCachedRecipe = createNewCachedRecipe(foundRecipe, process);
            if (newCachedRecipe == null) {
                //If we want to update the cache but failed to create a new cache then return that the item is not valid for the slot as something goes wrong
                // I believe we can actually make createNewCachedRecipe Nonnull which will remove this if statement
                return false;
            }
            updateCachedRecipe(newCachedRecipe, process);
        }
        return true;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<CombinerRecipe> getRecipeType() {
        return MekanismRecipeType.COMBINING;
    }

    @Nullable
    @Override
    public CombinerRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inventory.get(getInputSlot(cacheIndex));
        if (stack.isEmpty()) {
            return null;
        }
        ItemStack extra = inventory.get(EXTRA_SLOT_ID);
        return extra.isEmpty() ? null : findFirstRecipe(recipe -> recipe.test(stack, extra));
    }

    @Override
    public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@Nonnull CombinerRecipe recipe, int cacheIndex) {
        int inputSlot = getInputSlot(cacheIndex);
        int outputSlot = getOutputSlot(cacheIndex);
        return new CombinerCachedRecipe(recipe, InputHelper.getInputHandler(inventory, inputSlot), InputHelper.getInputHandler(inventory, EXTRA_SLOT_ID),
              OutputHelper.getOutputHandler(inventory, outputSlot))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
    }
}