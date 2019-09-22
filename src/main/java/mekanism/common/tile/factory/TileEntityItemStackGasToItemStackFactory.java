package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.ChemicalAction;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackGasToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;

//Compressing, injecting, purifying
public class TileEntityItemStackGasToItemStackFactory extends TileEntityFactory<ItemStackGasToItemStackRecipe> implements IGasHandler {

    //TODO: Finish moving references to gasTank to this class
    //public final GasTank gasTank;

    public TileEntityItemStackGasToItemStackFactory(IBlockProvider blockProvider) {
        super(blockProvider);
        //gasTank = new GasTank(TileEntityAdvancedElectricMachine.MAX_GAS * tier.processes);
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
        Gas gasFromItem = gasStackFromItem.getType();
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
            if (cachedRecipe.getItemInput().testType(fallbackInput) && (gasTank.isEmpty() || cachedRecipe.getGasInput().testType(gasTank.getType()))) {
                //Our input matches the recipe we have cached for this slot
                return true;
            }
            //If there is no cached item input or it doesn't match our fallback then it is an out of date cache, so we ignore the fact that we have a cache
        }
        //TODO: Decide if recipe.getOutput *should* assume that it is given a valid input or not
        // Here we are using it as if it is not assuming it, but that is in part because it currently does not care about the value passed
        // and if something does have extra checking to check the input as long as it checks for invalid ones this should still work
        GasStack gasStack = gasTank.getStack();
        Gas gas = gasStack.getType();
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

    public boolean isValidGas(@Nonnull Gas gas) {
        return getRecipes().contains(recipe -> recipe.getGasInput().testType(gas));
    }

    @Override
    protected void handleSecondaryFuel() {
        ItemStack extra = getInventory().get(EXTRA_SLOT_ID);
        if (!extra.isEmpty() && gasTank.getNeeded() > 0) {
            GasStack gasStack = GasConversionHandler.getItemGas(extra, gasTank, this::isValidGas);
            if (!gasStack.isEmpty()) {
                Gas gas = gasStack.getType();
                if (gasTank.canReceive(gas) && gasTank.getNeeded() >= gasStack.getAmount()) {
                    if (extra.getItem() instanceof IGasItem) {
                        IGasItem item = (IGasItem) extra.getItem();
                        gasTank.fill(item.removeGas(extra, gasStack.getAmount()), ChemicalAction.EXECUTE);
                    } else {
                        gasTank.fill(gasStack, ChemicalAction.EXECUTE);
                        extra.shrink(1);
                    }
                }
            }
        }
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
        GasStack gasStack = gasTank.getStack();
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

    public int getScaledGasLevel(int i) {
        return gasTank.getStored() * i / gasTank.getCapacity();
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("gasTank", gasTank.write(new CompoundNBT()));
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        gasTank.read(nbtTags.getCompound("gasTank"));
        GasUtils.clearIfInvalid(gasTank, this::isValidGas);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        super.writeSustainedData(itemStack);
        GasUtils.writeSustainedData(gasTank, itemStack);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        super.readSustainedData(itemStack);
        GasUtils.readSustainedData(gasTank, itemStack);
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, ChemicalAction action) {
        if (canReceiveGas(side, stack.getType())) {
            return gasTank.fill(stack, action);
        }
        return 0;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return configComponent.getOutput(TransmissionType.GAS, side, getDirection()).hasSlot(0) && gasTank.canReceiveType(type) && isValidGas(type);
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, ChemicalAction action) {
        return GasStack.EMPTY;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{gasTank};
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }
}