package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasItem;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasInventorySlot extends BasicInventorySlot {

    //TODO: Fix improper use of getStack() in TileEntityGasGenerator and TileEntityGasTank
    // This issue also sort of exists in the rotary condensentrator due to implementing the gas slots differently then intended

    /**
     * Gets the GasStack from ItemStack conversion, ignoring the size of the item stack.
     */
    private static GasStack getPotentialConversion(@Nullable World world, ItemStack itemStack) {
        ItemStackToGasRecipe foundRecipe = MekanismRecipeType.GAS_CONVERSION.findFirst(world, recipe -> recipe.getInput().testType(itemStack));
        return foundRecipe == null ? GasStack.EMPTY : foundRecipe.getOutput(itemStack);
    }

    /**
     * Fills/Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static GasInventorySlot rotary(GasTank gasTank, Predicate<@NonNull Gas> isValidGas, BooleanSupplier modeSupplier, @Nullable IMekanismInventory inventory,
          int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(isValidGas, "Gas validity check cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        //Mode == true if gas to fluid
        return new GasInventorySlot(gasTank, isValidGas, alwaysFalse, stack -> {
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
                return mode && isValidGas.test(gasContained.getType()) && gasTank.fill(gasContained, Action.SIMULATE) > 0;
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
                    return !containedGas.isEmpty() && isValidGas.test(containedGas.getType());
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
    public static GasInventorySlot fillOrConvert(GasTank gasTank, Predicate<@NonNull Gas> isValidGas, Supplier<World> worldSupplier, @Nullable IMekanismInventory inventory,
          int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(isValidGas, "Gas validity check cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new GasInventorySlot(gasTank, isValidGas, worldSupplier, stack -> {
            //NOTE: Even though we KNOW from isValid when we added the item that this should be an IGasItem, have it double check until we end up switching to a capability
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                //Only allow extraction if our item is out of gas
                return ((IGasItem) item).getGas(stack).isEmpty();
            }
            //Always allow extraction if something went horribly wrong and we are not an IGasItem AND we can't provide a valid type of gas
            // This might happen after a reload for example
            //TODO: Should we be checking the tank for if the gas is valid?
            GasStack gasConversion = getPotentialConversion(worldSupplier.get(), stack);
            return gasConversion.isEmpty() || !isValidGas.test(gasConversion.getType());
        }, stack -> {
            //NOTE: Even though we KNOW from isValid that this should be an IGasItem, have it double check until we end up switching to a capability
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                GasStack containedGas = ((IGasItem) item).getGas(stack);
                //True if we can fill the tank with any of our contents, ignored if the item has no gas, as it won't pass isValid
                return gasTank.fill(containedGas, Action.SIMULATE) > 0;
            }
            GasStack gasConversion = getPotentialConversion(worldSupplier.get(), stack);
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            return !gasConversion.isEmpty() && isValidGas.test(gasConversion.getType()) && gasTank.fill(gasConversion, Action.SIMULATE) > 0;
        }, stack -> {
            Item item = stack.getItem();
            //TODO: Use a capability instead of instanceof
            if (item instanceof IGasItem) {
                //TODO: Add a way to the capability to see if the item can ever output gas, as things like jetpacks cannot have the gas be drained from them
                // Strictly speaking this currently could be done as gasItem.canProvideGas(stack, MekanismAPI.EMPTY_GAS), but is being ignored instead for clarity
                GasStack containedGas = ((IGasItem) item).getGas(stack);
                return !containedGas.isEmpty() && isValidGas.test(containedGas.getType());
            }
            //Allow gas conversion of items that have a gas that is valid
            GasStack gasConversion = getPotentialConversion(worldSupplier.get(), stack);
            return !gasConversion.isEmpty() && isValidGas.test(gasConversion.getType());
        }, inventory, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static GasInventorySlot fill(GasTank gasTank, Predicate<@NonNull Gas> isValidGas, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(isValidGas, "Gas validity check cannot be null");
        return new GasInventorySlot(gasTank, isValidGas, stack -> {
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
    public static GasInventorySlot drain(GasTank gasTank, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
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

    private final Predicate<@NonNull Gas> isValidGas;
    //TODO: Do we want to make other things than just the conversion one have a world supplier?
    // Currently it is the only one that actually needs it
    private final Supplier<World> worldSupplier;
    //TODO: Replace GasTank with an IGasHandler??
    private final GasTank gasTank;

    private GasInventorySlot(GasTank gasTank, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator,
          @Nullable IMekanismInventory inventory, int x, int y) {
        //TODO: Decide if this should be always true or always false for being a valid gas. This is current only used by the draining method
        this(gasTank, gas -> false, canExtract, canInsert, validator, inventory, x, y);
    }

    private GasInventorySlot(GasTank gasTank, Predicate<@NonNull Gas> isValidGas, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        this(gasTank, isValidGas, () -> null, canExtract, canInsert, validator, inventory, x, y);
    }

    private GasInventorySlot(GasTank gasTank, Predicate<@NonNull Gas> isValidGas, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        this.gasTank = gasTank;
        this.isValidGas = isValidGas;
        this.worldSupplier = worldSupplier;
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.EXTRA;
    }

    /**
     * Fills tank from slot, allowing for the item to also be converted to gas if need be
     */
    public void fillTankOrConvert() {
        if (!isEmpty() && gasTank.getNeeded() > 0) {
            //Fill the tank from the item
            if (!fillTankFromItem()) {
                //If filling from item failed, try doing it by conversion
                ItemStackToGasRecipe foundRecipe = MekanismRecipeType.GAS_CONVERSION.findFirst(worldSupplier.get(), recipe -> recipe.getInput().test(current));
                if (foundRecipe != null) {
                    ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                    if (!itemInput.isEmpty()) {
                        GasStack output = foundRecipe.getOutput(itemInput);
                        if (!output.isEmpty() && gasTank.canReceive(output) && isValidGas.test(output.getType())) {
                            gasTank.fill(output, Action.EXECUTE);
                            int amountUsed = itemInput.getCount();
                            if (shrinkStack(amountUsed, Action.EXECUTE) != amountUsed) {
                                //TODO: Print warning/error
                            }
                            onContentsChanged();
                        }
                    }
                }
            }
        }
    }

    /**
     * Fills tank from slot, does not try converting the item via gas conversion
     */
    public void fillTank() {
        if (!isEmpty() && gasTank.getNeeded() > 0) {
            //Try filling from the tank's item
            fillTankFromItem();
        }
    }

    /**
     * @implNote Does not pre-check if the current stack is empty or that the gas tank needs gas
     */
    private boolean fillTankFromItem() {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's IGasItem's stack so at the moment it doesn't fully matter
        if (current.getItem() instanceof IGasItem) {
            IGasItem item = (IGasItem) current.getItem();
            GasStack gasInItem = item.getGas(current);
            //Check to make sure it can provide the gas it contains
            if (!gasInItem.isEmpty()) {
                Gas gas = gasInItem.getType();
                if (item.canProvideGas(current, gas) && gasTank.canReceiveType(gas)) {
                    int amount = Math.min(gasTank.getNeeded(), Math.min(gasInItem.getAmount(), item.getRate(current)));
                    if (amount > 0 && isValidGas.test(gas)) {
                        gasTank.fill(item.removeGas(current, amount), Action.EXECUTE);
                        onContentsChanged();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Drains tank into slot
     */
    public void drainTank() {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's IGasItem's stack so at the moment it doesn't fully matter
        if (!isEmpty() && !gasTank.isEmpty()) {
            //TODO: Capability for gas item, and then rewrite this so it simulates and things
            if (current.getItem() instanceof IGasItem) {
                IGasItem gasItem = (IGasItem) current.getItem();
                GasStack storedGas = gasTank.getStack();
                if (gasItem.canReceiveGas(current, storedGas.getType())) {
                    int amount = Math.min(gasItem.getNeeded(current), gasItem.getRate(current));
                    if (amount > 0) {
                        GasStack drained = gasTank.drain(amount, Action.SIMULATE);
                        if (!drained.isEmpty()) {
                            int amountAccepted = gasItem.addGas(current, drained);
                            gasTank.drain(amountAccepted, Action.EXECUTE);
                            onContentsChanged();
                        }
                    }
                }
            }
        }
    }
}