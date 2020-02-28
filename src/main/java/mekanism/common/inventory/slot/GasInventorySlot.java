package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasInventorySlot extends BasicInventorySlot {

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
    public static GasInventorySlot rotary(IChemicalTank<Gas, GasStack> gasTank, BooleanSupplier modeSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        //TODO: Make there be a fill/drain version that just based on the mode doesn't allow inserting/extracting
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        //Mode == true if fluid to gas
        return new GasInventorySlot(gasTank, alwaysFalse, stack -> {
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                boolean mode = modeSupplier.getAsBoolean();
                boolean allEmpty = true;
                for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                    GasStack gasInTank = gasHandlerItem.getGasInTank(tank);
                    if (!gasInTank.isEmpty()) {
                        if (gasTank.insert(gasInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < gasInTank.getAmount()) {
                            //True if we are the input tank and the items contents are valid and can fill the tank with any of our contents
                            return mode;
                        }
                        allEmpty = false;
                    }
                }
                //We want to try and drain the tank AND we are not the input tank
                return allEmpty && mode;
            }
            return false;
        }, stack -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent(), inventory, x, y);
    }

    /**
     * Fills the tank from this item OR converts the given item to a gas
     */
    public static GasInventorySlot fillOrConvert(IChemicalTank<Gas, GasStack> gasTank, Supplier<World> worldSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new GasInventorySlot(gasTank, worldSupplier, stack -> {
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                    if (gasTank.isValid(gasHandlerItem.getGasInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //Only allow extraction if our item is out of gas
                return true;
            }
            //Always allow extraction if something went horribly wrong and we are not a gas item AND we can't provide a valid type of gas
            // This might happen after a reload for example
            GasStack gasConversion = getPotentialConversion(worldSupplier.get(), stack);
            return gasConversion.isEmpty() || !gasTank.isValid(gasConversion);
        }, stack -> {
            if (fillInsertCheck(gasTank, stack)) {
                return true;
            }
            GasStack gasConversion = getPotentialConversion(worldSupplier.get(), stack);
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            return !gasConversion.isEmpty() && gasTank.insert(gasConversion, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < gasConversion.getAmount();
        }, stack -> {
            if (stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent()) {
                //Note: we mark all gas items as valid and have a more restrictive insert check so that we allow full tanks when they are done being filled
                return true;
            }
            //Allow gas conversion of items that have a gas that is valid
            GasStack gasConversion = getPotentialConversion(worldSupplier.get(), stack);
            return !gasConversion.isEmpty() && gasTank.isValid(gasConversion);
        }, inventory, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static GasInventorySlot fill(IChemicalTank<Gas, GasStack> gasTank, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        return new GasInventorySlot(gasTank, stack -> {
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                    if (gasTank.isValid(gasHandlerItem.getGasInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //If we have no contents that are still valid, allow extraction
            }
            //Always allow it if something went horribly wrong and we are not a gas item
            return true;
        }, stack -> fillInsertCheck(gasTank, stack), stack -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent(), inventory, x, y);
    }

    private static boolean fillInsertCheck(IChemicalTank<Gas, GasStack> gasTank, @NonNull ItemStack stack) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                GasStack gasInTank = gasHandlerItem.getGasInTank(tank);
                if (!gasInTank.isEmpty() && gasTank.insert(gasInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < gasInTank.getAmount()) {
                    //True if we can fill the tank with any of our contents
                    // Note: We need to recheck the fact the gas is not empty in case the item has multiple tanks and only some of the fluids are valid
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Accepts any items that can be filled with the current contents of the gas tank, or if it is a gas tank container and the tank is currently empty
     *
     * Drains the tank into this item.
     */
    public static GasInventorySlot drain(IChemicalTank<Gas, GasStack> gasTank, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Predicate<@NonNull ItemStack> insertPredicate = stack -> {
            Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                if (gasTank.isEmpty()) {
                    //If the gas tank is empty, accept the gas item  as long as it is not full
                    for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                        if (gasHandlerItem.getGasInTank(tank).getAmount() < gasHandlerItem.getGasTankCapacity(tank)) {
                            //True if we have any space in this tank
                            return true;
                        }
                    }
                    return false;
                }
                //Otherwise if we can accept any of the gas that is currently stored in the tank, then we allow inserting the item
                return gasHandlerItem.insertGas(gasTank.getStack(), Action.SIMULATE).getAmount() < gasTank.getStored();
            }
            return false;
        };
        return new GasInventorySlot(gasTank, insertPredicate.negate(), insertPredicate, stack -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent(), inventory, x, y);
    }

    //TODO: Do we want to make other things than just the conversion one have a world supplier?
    // Currently it is the only one that actually needs it
    private final Supplier<World> worldSupplier;
    private final IChemicalTank<Gas, GasStack> gasTank;

    private GasInventorySlot(IChemicalTank<Gas, GasStack> gasTank, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        this(gasTank, () -> null, canExtract, canInsert, validator, inventory, x, y);
    }

    private GasInventorySlot(IChemicalTank<Gas, GasStack> gasTank, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.gasTank = gasTank;
        this.worldSupplier = worldSupplier;
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
                        if (!output.isEmpty() && gasTank.insert(output, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                            //If we can accept it all, then add it and decrease our input
                            int amount = output.getAmount();
                            if (gasTank.shrinkStack(amount, Action.EXECUTE) != amount) {
                                //TODO: Print warning/error
                            }
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
        // Note: None of Mekanism's gas items stack so at the moment it doesn't fully matter
        Optional<IGasHandler> capability = MekanismUtils.toOptional(current.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            boolean didTransfer = false;
            for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                GasStack gasInItem = gasHandlerItem.getGasInTank(tank);
                if (!gasInItem.isEmpty()) {
                    //Simulate inserting gas from each tank in the item into our tank
                    GasStack simulatedRemainder = gasTank.insert(gasInItem, Action.SIMULATE, AutomationType.INTERNAL);
                    int gasInItemAmount = gasInItem.getAmount();
                    int remainder = simulatedRemainder.getAmount();
                    if (remainder < gasInItemAmount) {
                        //If we were simulated that we could actually insert any, then
                        // extract up to as much gas as we were able to accept from the item
                        GasStack extractedGas = gasHandlerItem.extractGas(tank, gasInItemAmount - remainder, Action.EXECUTE);
                        if (!extractedGas.isEmpty()) {
                            //If we were able to actually extract it from the item, then insert it into our gas tank
                            if (!gasTank.insert(extractedGas, Action.EXECUTE, AutomationType.INTERNAL).isEmpty()) {
                                //TODO: Print warning/error
                            }
                            //and mark that we were able to transfer at least some of it
                            didTransfer = true;
                            if (gasTank.getNeeded() == 0) {
                                //If our tank is full then exit early rather than continuing
                                // to check about filling the tank from the item
                                break;
                            }
                        }
                    }
                }
            }
            if (didTransfer) {
                onContentsChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * Drains tank into slot
     */
    public void drainTank() {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's gas items stack so at the moment it doesn't fully matter
        if (!isEmpty() && !gasTank.isEmpty()) {
            Optional<IGasHandler> capability = MekanismUtils.toOptional(current.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                GasStack storedGas = gasTank.getStack();
                GasStack simulatedRemainder = gasHandlerItem.insertGas(storedGas, Action.SIMULATE);
                int remainder = simulatedRemainder.getAmount();
                int amount = storedGas.getAmount();
                if (remainder < amount) {
                    //We are able to fit at least some of the gas from our tank into the item
                    GasStack extractedGas = gasTank.extract(amount - remainder, Action.EXECUTE, AutomationType.INTERNAL);
                    if (!extractedGas.isEmpty()) {
                        //If we were able to actually extract it from our tank, then insert it into the item
                        if (!gasHandlerItem.insertGas(extractedGas, Action.EXECUTE).isEmpty()) {
                            //TODO: Print warning/error
                        }
                        onContentsChanged();
                    }
                }
            }
        }
    }
}