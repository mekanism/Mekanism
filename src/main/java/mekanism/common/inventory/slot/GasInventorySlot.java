package mekanism.common.inventory.slot;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasItem;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.recipe.GasConversionHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GasInventorySlot extends BasicInventorySlot {

    /**
     * Fills/Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static GasInventorySlot rotary(@Nonnull GasTank gasTank, Predicate<@NonNull Gas> validInput, BooleanSupplier modeSupplier, IMekanismInventory inventory,
          int x, int y) {
        //Mode == true if gas to fluid
        return new GasInventorySlot(gasTank, alwaysFalse, stack -> {
            //NOTE: Even though we KNOW from isValid when we added the item that this should be an IGasItem, have it double check until we end up switching to a capability
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                boolean mode = modeSupplier.getAsBoolean();
                GasStack gasContained = ((IGasItem) item).getGas(stack);
                if (gasContained.isEmpty()) {
                    //We want to try and drain the tank AND we are not the input tank
                    return !mode;
                }
                //True if we are the input tank and the items contents are valid and can fill the tank with any of our contents
                return mode && validInput.test(gasContained.getType()) && gasTank.fill(gasContained, Action.SIMULATE) > 0;
            }
            return false;
        }, stack -> {
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                IGasItem gasItem = (IGasItem) item;
                if (modeSupplier.getAsBoolean()) {
                    //Input tank, so we want to fill it
                    //TODO: Add a way to the capability to see if the item can ever output gas, as things like jetpacks cannot have the gas be drained from them
                    // Strictly speaking this currently could be done as gasItem.canProvideGas(stack, MekanismAPI.EMPTY_GAS), but is being ignored instead for clarity
                    GasStack containedGas = gasItem.getGas(stack);
                    return !containedGas.isEmpty() && validInput.test(containedGas.getType());
                }
                //Output tank, so we want to drain
                //Only accept items that are gas items and can accept some form of gas
                return gasItem.getNeeded(stack) > 0;
            }
            return false;
        }, inventory, x, y);
    }

    /**
     * Fills the tank from this item OR converts the given item to a gas
     */
    public static GasInventorySlot fillOrConvert(@Nonnull GasTank gasTank, @Nonnull Predicate<Gas> isValidGas, IMekanismInventory inventory, int x, int y) {
        return new GasInventorySlot(gasTank, stack -> {
            //NOTE: Even though we KNOW from isValid when we added the item that this should be an IGasItem, have it double check until we end up switching to a capability
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                //Only allow extraction if our item is out of gas
                return ((IGasItem) item).getGas(stack).isEmpty();
            }
            //Always allow it if something went horribly wrong and we are not an IGasItem AND we can't provide a valid type of gas
            //TODO: Should we be checking the tank for if the gas is valid?
            //TODO: Re-evaluate this after switching GasConversion to being a recipe
            return GasConversionHandler.getItemGasConversion(stack, isValidGas).isEmpty();
        }, stack -> {
            //NOTE: Even though we KNOW from isValid that this should be an IGasItem, have it double check until we end up switching to a capability
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                GasStack containedGas = ((IGasItem) item).getGas(stack);
                //True if we can fill the tank with any of our contents, ignored if the item has no gas, as it won't pass isValid
                return gasTank.fill(containedGas, Action.SIMULATE) > 0;
            }
            //TODO: Re-evaluate this after switching GasConversion to being a recipe
            GasStack gasConversion = GasConversionHandler.getItemGasConversion(stack, isValidGas);
            //Note: We recheck about this being empty as the conversion list might have changed
            return !gasConversion.isEmpty() && gasTank.fill(gasConversion, Action.SIMULATE) > 0;
        }, stack -> {
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                //TODO: Add a way to the capability to see if the item can ever output gas, as things like jetpacks cannot have the gas be drained from them
                // Strictly speaking this currently could be done as gasItem.canProvideGas(stack, MekanismAPI.EMPTY_GAS), but is being ignored instead for clarity
                GasStack containedGas = ((IGasItem) item).getGas(stack);
                return !containedGas.isEmpty() && isValidGas.test(containedGas.getType());
            }
            //TODO: Re-evaluate this after switching GasConversion to being a recipe
            //Allow gas conversion of items that have a gas that is valid
            return !GasConversionHandler.getItemGasConversion(stack, isValidGas).isEmpty();
        }, inventory, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static GasInventorySlot fill(@Nonnull GasTank gasTank, @Nonnull Predicate<Gas> isValidGas, IMekanismInventory inventory, int x, int y) {
        return new GasInventorySlot(gasTank, stack -> {
            //NOTE: Even though we KNOW from isValid when we added the item that this should be an IGasItem, have it double check until we end up switching to a capability
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                //Only allow extraction if our item is out of gas
                return ((IGasItem) item).getGas(stack).isEmpty();
            }
            //Always allow it if something went horribly wrong and we are not an IGasItem
            return true;
        }, stack -> {
            //NOTE: Even though we KNOW from isValid that this should be an IGasItem, have it double check until we end up switching to a capability
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                GasStack containedGas = ((IGasItem) item).getGas(stack);
                //True if we can fill the tank with any of our contents, ignored if the item has no gas, as it won't pass isValid
                return gasTank.fill(containedGas, Action.SIMULATE) > 0;
            }
            return false;
        }, stack -> {
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                //TODO: Add a way to the capability to see if the item can ever output gas, as things like jetpacks cannot have the gas be drained from them
                // Strictly speaking this currently could be done as gasItem.canProvideGas(stack, MekanismAPI.EMPTY_GAS), but is being ignored instead for clarity
                GasStack containedGas = ((IGasItem) item).getGas(stack);
                return !containedGas.isEmpty() && isValidGas.test(containedGas.getType());
            }
            return false;
        }, inventory, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the gas tank, or if it is a gas tank container and the tank is currently empty
     *
     * Drains the tank into this item.
     */
    public static GasInventorySlot drain(@Nonnull GasTank gasTank, IMekanismInventory inventory, int x, int y) {
        return new GasInventorySlot(gasTank, stack -> {
            //NOTE: Even though we KNOW from isValid that this should be an IGasItem, have it double check until we end up switching to a capability
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                //Only allow extraction if our item is full
                return ((IGasItem) item).getNeeded(stack) == 0;
            }
            //Always allow it if something went horribly wrong and we are not an IGasItem
            return true;
        }, stack -> {
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                IGasItem gasItem = (IGasItem) item;
                GasStack containedGas = gasItem.getGas(stack);
                //TODO: After switching to caps use simulations to see if the item can accept a given gas so that this would become
                // gasTank.isEmpty() OR it simulating the item accepting the gas.
                if (containedGas.isEmpty()) {
                    return true;
                }
                //NOTE: The canReceiveGas is not consistent on if it checks if we need any gas or we even double check the contained type
                return gasTank.isEmpty() || gasItem.canReceiveGas(stack, gasTank.getType());
            }
            return false;
        }, stack -> {
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                //Only accept items that are gas items and can accept some form of gas
                return ((IGasItem) item).getNeeded(stack) > 0;
            }
            return false;
        }, inventory, x, y);
    }

    //TODO: Replace GasTank with an IGasHandler??
    private final GasTank gasTank;

    private GasInventorySlot(@Nonnull GasTank gasTank, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          @Nonnull Predicate<@NonNull ItemStack> validator, IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        this.gasTank = gasTank;
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.EXTRA;
    }

    //TODO: Make it so that the gas tank drains/fills
}