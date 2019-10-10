package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.SawmillCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntitySawingFactory extends TileEntityFactory<SawmillRecipe> {

    protected final IInputHandler<@NonNull ItemStack>[] inputHandlers;
    protected final IOutputHandler<@NonNull ChanceOutput>[] outputHandlers;

    public TileEntitySawingFactory(IBlockProvider blockProvider) {
        super(blockProvider);
        inputHandlers = new IInputHandler[tier.processes];
        outputHandlers = new IOutputHandler[tier.processes];
        for (int i = 0; i < tier.processes; i++) {
            inputHandlers[i] = InputHelper.getInputHandler(this, getInputSlot(i));
            outputHandlers[i] = OutputHelper.getOutputHandler(this, getOutputSlot(i), EXTRA_SLOT_ID);
        }
    }

    @Override
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return containsRecipe(recipe -> recipe.getInput().testType(stack));
    }

    @Override
    public boolean isValidExtraItem(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean inputProducesOutput(int slotID, ItemStack fallbackInput, ItemStack output, boolean updateCache) {
        if (output.isEmpty()) {
            return true;
        }
        int process = getOperation(slotID);
        CachedRecipe<SawmillRecipe> cached = getCachedRecipe(process);
        if (cached != null && cached.getRecipe().getInput().testType(fallbackInput)) {
            return true;
        }
        //If there is no cached item input or it doesn't match our fallback then it is an out of date cache, so we ignore the fact that we have a cache
        //TODO: Decide if recipe.getOutput *should* assume that it is given a valid input or not
        // Here we are using it as if it is not assuming it, but that is in part because it currently does not care about the value passed
        // and if something does have extra checking to check the input as long as it checks for invalid ones this should still work
        ItemStack extra = getStackInSlot(EXTRA_SLOT_ID);
        SawmillRecipe foundRecipe = findFirstRecipe(recipe -> {
            if (recipe.getInput().testType(fallbackInput)) {
                ChanceOutput chanceOutput = recipe.getOutput(fallbackInput);
                if (ItemHandlerHelper.canItemStacksStack(chanceOutput.getMainOutput(), output)) {
                    //If the input is good and the primary output matches, make sure that the secondary
                    // output of this recipe will stack with what is currently in the secondary slot
                    if (extra.isEmpty()) {
                        return true;
                    }
                    ItemStack secondaryOutput = chanceOutput.getMaxSecondaryOutput();
                    return secondaryOutput.isEmpty() || ItemHandlerHelper.canItemStacksStack(secondaryOutput, extra);
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
            CachedRecipe<SawmillRecipe> newCachedRecipe = createNewCachedRecipe(foundRecipe, process);
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
    public MekanismRecipeType<SawmillRecipe> getRecipeType() {
        return MekanismRecipeType.SAWING;
    }

    @Nullable
    @Override
    public SawmillRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandlers[cacheIndex].getInput();
        if (stack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack));
    }

    @Override
    public CachedRecipe<SawmillRecipe> createNewCachedRecipe(@Nonnull SawmillRecipe recipe, int cacheIndex) {
        return new SawmillCachedRecipe(recipe, inputHandlers[cacheIndex], outputHandlers[cacheIndex])
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        return slotID == EXTRA_SLOT_ID || super.canExtractItem(slotID, itemstack, side);
    }
}