package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackGasToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

//Compressing, injecting, purifying
public class TileEntityItemStackGasToItemStackFactory extends TileEntityFactory<ItemStackGasToItemStackRecipe> {

    /**
     * How much secondary energy each operation consumes per tick
     */
    private double secondaryEnergyPerTick = 0;
    private int secondaryEnergyThisTick;

    public TileEntityItemStackGasToItemStackFactory(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return getRecipes().contains(recipe -> recipe.getItemInput().testType(stack));
    }

    @Override
    public boolean isValidExtraItem(@Nonnull ItemStack stack) {
        GasStack gasStackFromItem = GasConversionHandler.getItemGas(stack, gasTank, this::isValidGas);
        if (gasStackFromItem.isEmpty()) {
            return false;
        }
        Gas gasFromItem = gasStackFromItem.getGas();
        return getRecipes().contains(recipe -> recipe.getGasInput().testType(gasFromItem));
    }

    @Override
    public boolean inputProducesOutput(int slotID, ItemStack fallbackInput, ItemStack output, boolean updateCache) {
        if (output.isEmpty()) {
            return true;
        }
        int process = getOperation(slotID);
        CachedRecipe<ItemStackGasToItemStackRecipe> cached = getCachedRecipe(process);
        if (cached != null) {
            ItemStackGasToItemStackRecipe cachedRecipe = cached.getRecipe();
            if (cachedRecipe.getItemInput().testType(fallbackInput) && (gasTank.isEmpty() || cachedRecipe.getGasInput().testType(gasTank.getGasType()))) {
                //Our input matches the recipe we have cached for this slot
                return true;
            }
            //If there is no cached item input or it doesn't match our fallback then it is an out of date cache, so we ignore the fact that we have a cache
        }
        //TODO: Decide if recipe.getOutput *should* assume that it is given a valid input or not
        // Here we are using it as if it is not assuming it, but that is in part because it currently does not care about the value passed
        // and if something does have extra checking to check the input as long as it checks for invalid ones this should still work
        GasStack gasStack = gasTank.getGas();
        Gas gas = gasStack.getGas();
        ItemStackGasToItemStackRecipe foundRecipe = getRecipes().findFirst(recipe -> {
            if (recipe.getItemInput().testType(fallbackInput)) {
                //If we don't have a gas stored ignore checking for a match
                if (gasStack.isEmpty() || recipe.getGasInput().testType(gas)) {
                    //TODO: Give it something that is not null when we don't have a stored gas stack
                    return ItemHandlerHelper.canItemStacksStack(recipe.getOutput(fallbackInput, gasStack), output);
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
            CachedRecipe<ItemStackGasToItemStackRecipe> newCachedRecipe = createNewCachedRecipe(foundRecipe, process);
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
    public boolean isValidGas(@Nonnull Gas gas) {
        return getRecipes().contains(recipe -> recipe.getGasInput().testType(gas));
    }

    @Nonnull
    @Override
    public Recipe<ItemStackGasToItemStackRecipe> getRecipes() {
        switch (type) {
            case INJECTING:
                return Recipe.CHEMICAL_INJECTION_CHAMBER;
            case PURIFYING:
                return Recipe.PURIFICATION_CHAMBER;
            case COMPRESSING:
            default:
                //TODO: Make it so that it throws an error if it is not one of the three types
                return Recipe.OSMIUM_COMPRESSOR;
        }
    }

    @Nullable
    @Override
    public ItemStackGasToItemStackRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inventory.get(getInputSlot(cacheIndex));
        if (stack.isEmpty()) {
            return null;
        }
        GasStack gasStack = gasTank.getGas();
        return gasStack.isEmpty() ? null : getRecipes().findFirst(recipe -> recipe.test(stack, gasStack));
    }

    @Override
    public CachedRecipe<ItemStackGasToItemStackRecipe> createNewCachedRecipe(@Nonnull ItemStackGasToItemStackRecipe recipe, int cacheIndex) {
        int inputSlot = getInputSlot(cacheIndex);
        int outputSlot = getOutputSlot(cacheIndex);
        return new ItemStackGasToItemStackCachedRecipe(recipe, InputHelper.getInputHandler(inventory, inputSlot), InputHelper.getInputHandler(gasTank),
              () -> secondaryEnergyThisTick, OutputHelper.getOutputHandler(inventory, outputSlot))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
    }
}