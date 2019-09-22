package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.MetallurgicInfuserCachedRecipe;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityMetallurgicInfuserFactory extends TileEntityFactory<MetallurgicInfuserRecipe> {

    public TileEntityMetallurgicInfuserFactory(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return getRecipes().contains(recipe -> recipe.getItemInput().testType(stack));
    }

    @Override
    public boolean isValidExtraItem(@Nonnull ItemStack stack) {
        InfusionStack infuse = InfuseRegistry.getObject(stack);
        return !infuse.isEmpty() && Recipe.METALLURGIC_INFUSER.contains(recipe -> recipe.getInfusionInput().testType(infuse.getType()));
    }

    @Override
    public boolean inputProducesOutput(int slotID, ItemStack fallbackInput, ItemStack output, boolean updateCache) {
        if (output.isEmpty()) {
            return true;
        }
        int process = getOperation(slotID);
        CachedRecipe<MetallurgicInfuserRecipe> cached = getCachedRecipe(process);
        if (cached != null) {
            MetallurgicInfuserRecipe cachedRecipe = cached.getRecipe();
            if (cachedRecipe.getItemInput().testType(fallbackInput) && (infuseStored.isEmpty() || cachedRecipe.getInfusionInput().testType(infuseStored.getType()))) {
                //Our input matches the recipe we have cached for this slot
                return true;
            }
            //If there is no cached item input or it doesn't match our fallback then it is an out of date cache, so we ignore the fact that we have a cache
        }
        //TODO: Decide if recipe.getOutput *should* assume that it is given a valid input or not
        // Here we are using it as if it is not assuming it, but that is in part because it currently does not care about the value passed
        // and if something does have extra checking to check the input as long as it checks for invalid ones this should still work
        int stored = infuseStored.getAmount();
        InfuseType type = infuseStored.getType();
        MetallurgicInfuserRecipe foundRecipe = getRecipes().findFirst(recipe -> {
            //Check the infusion type before the ItemStack type as it a quicker easier compare check
            if (stored == 0 || recipe.getInfusionInput().testType(type)) {
                return recipe.getItemInput().testType(fallbackInput) && ItemHandlerHelper.canItemStacksStack(recipe.getOutput(infuseStored.getStack(), fallbackInput), output);
            }
            return false;
        });
        if (foundRecipe == null) {
            //We could not find any valid recipe for the given item that matches the items in the current output slots
            return false;
        }
        if (updateCache) {
            //If we want to update the cache, then create a new cache with the recipe we found
            CachedRecipe<MetallurgicInfuserRecipe> newCachedRecipe = createNewCachedRecipe(foundRecipe, process);
            if (newCachedRecipe == null) {
                //If we want to update the cache but failed to create a new cache then return that the item is not valid for the slot as something goes wrong
                // I believe we can actually make createNewCachedRecipe Nonnull which will remove this if statement
                return false;
            }
            updateCachedRecipe(newCachedRecipe, process);
        }
        return true;
    }

    @Override
    protected void handleSecondaryFuel() {
        ItemStack extra = getInventory().get(EXTRA_SLOT_ID);
        if (!extra.isEmpty()) {
            InfusionStack pendingInfusionInput = InfuseRegistry.getObject(extra);
            if (!pendingInfusionInput.isEmpty()) {
                if (infuseStored.isEmpty() || infuseStored.getType() == pendingInfusionInput.getType()) {
                    if (infuseStored.getAmount() + pendingInfusionInput.getAmount() <= maxInfuse) {
                        infuseStored.increase(pendingInfusionInput);
                        extra.shrink(1);
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public Recipe<MetallurgicInfuserRecipe> getRecipes() {
        return Recipe.METALLURGIC_INFUSER;
    }

    @Nullable
    @Override
    public MetallurgicInfuserRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inventory.get(getInputSlot(cacheIndex));
        return stack.isEmpty() ? null : getRecipes().findFirst(recipe -> recipe.test(infuseStored.getStack(), stack));
    }

    @Override
    public CachedRecipe<MetallurgicInfuserRecipe> createNewCachedRecipe(@Nonnull MetallurgicInfuserRecipe recipe, int cacheIndex) {
        int inputSlot = getInputSlot(cacheIndex);
        int outputSlot = getOutputSlot(cacheIndex);
        return new MetallurgicInfuserCachedRecipe(recipe, InputHelper.getInputHandler(infuseStored), InputHelper.getInputHandler(inventory, inputSlot),
              OutputHelper.getOutputHandler(inventory, outputSlot))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
    }
}