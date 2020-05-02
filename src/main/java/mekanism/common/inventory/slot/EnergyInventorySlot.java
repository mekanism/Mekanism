package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyInventorySlot extends BasicInventorySlot {

    /**
     * Gets the energy from ItemStack conversion, ignoring the size of the item stack.
     */
    private static FloatingLong getPotentialConversion(@Nullable World world, ItemStack itemStack) {
        ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.findFirst(world, recipe -> recipe.getInput().testType(itemStack));
        return foundRecipe == null ? FloatingLong.ZERO : foundRecipe.getOutput(itemStack);
    }

    /**
     * Fills the container from this item OR converts the given item to energy
     */
    public static EnergyInventorySlot fillOrConvert(IEnergyContainer energyContainer, Supplier<World> worldSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        Function<ItemStack, FloatingLong> potentialConversionSupplier = stack -> getPotentialConversion(worldSupplier.get(), stack);
        return new EnergyInventorySlot(energyContainer, worldSupplier, stack -> {
            //Always allow extraction if something went horribly wrong and we are not an energy container item or we are no longer a valid conversion
            // This might happen after a reload for example
            return !EnergyCompatUtils.hasStrictEnergyHandler(stack) && potentialConversionSupplier.apply(stack).isZero();
        }, stack -> {
            if (fillInsertCheck(stack)) {
                return true;
            }
            FloatingLong conversion = potentialConversionSupplier.apply(stack);
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            return !conversion.isZero() && energyContainer.insert(conversion, Action.SIMULATE, AutomationType.INTERNAL).smallerThan(conversion);
        }, stack -> {
            //Note: we mark all energy handler items as valid and have a more restrictive insert check so that we allow full containers when they are done being filled
            // We also allow energy conversion of items that can be converted
            return EnergyCompatUtils.hasStrictEnergyHandler(stack) || !getPotentialConversion(worldSupplier.get(), stack).isZero();
        }, inventory, x, y);
    }

    /**
     * Fills the container from this item
     */
    public static EnergyInventorySlot fill(IEnergyContainer energyContainer, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        return new EnergyInventorySlot(energyContainer, stack -> !EnergyCompatUtils.hasStrictEnergyHandler(stack), EnergyInventorySlot::fillInsertCheck,
              EnergyCompatUtils::hasStrictEnergyHandler, inventory, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the energy container, or if it is an energy container and the container is currently empty
     *
     * Drains the container into this item.
     */
    public static EnergyInventorySlot drain(IEnergyContainer energyContainer, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        Predicate<@NonNull ItemStack> insertPredicate = stack -> {
            IStrictEnergyHandler itemHandler = EnergyCompatUtils.getStrictEnergyHandler(stack);
            if (itemHandler == null) {
                return false;
            }
            if (energyContainer.isEmpty()) {
                //If the energy container is empty, accept the energy item as long as it is not full
                for (int container = 0; container < itemHandler.getEnergyContainerCount(); container++) {
                    if (!itemHandler.getNeededEnergy(container).isZero()) {
                        //True if we have any space in this container
                        return true;
                    }
                }
                return false;
            }
            //Otherwise if we can accept any energy that is currently stored in the container, then we allow inserting the item
            return itemHandler.insertEnergy(energyContainer.getEnergy(), Action.SIMULATE).smallerThan(energyContainer.getEnergy());
        };
        return new EnergyInventorySlot(energyContainer, insertPredicate.negate(), insertPredicate, EnergyCompatUtils::hasStrictEnergyHandler, inventory, x, y);
    }

    private static boolean fillInsertCheck(ItemStack stack) {
        IStrictEnergyHandler itemHandler = EnergyCompatUtils.getStrictEnergyHandler(stack);
        if (itemHandler != null) {
            for (int container = 0; container < itemHandler.getEnergyContainerCount(); container++) {
                FloatingLong energyInContainer = itemHandler.getEnergy(container);
                if (!energyInContainer.isZero()) {
                    return true;
                }
            }
        }
        return false;
    }

    private final Supplier<World> worldSupplier;
    private final IEnergyContainer energyContainer;

    private EnergyInventorySlot(IEnergyContainer energyContainer, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        this(energyContainer, () -> null, canExtract, canInsert, validator, inventory, x, y);
    }

    private EnergyInventorySlot(IEnergyContainer energyContainer, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        this.energyContainer = energyContainer;
        this.worldSupplier = worldSupplier;
        setSlotType(ContainerSlotType.POWER);
        setSlotOverlay(SlotOverlay.POWER);
    }

    /**
     * Fills the energy container from slot, allowing for the item to also be converted to energy if need be (example redstone -> energy)
     */
    public void fillContainerOrConvert() {
        if (!isEmpty() && !energyContainer.getNeeded().isZero()) {
            //Fill the container from the item
            if (!fillContainerFromItem()) {
                //If filling from item failed, try doing it by conversion
                ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.findFirst(worldSupplier.get(), recipe -> recipe.getInput().test(current));
                if (foundRecipe != null) {
                    ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                    if (!itemInput.isEmpty()) {
                        FloatingLong output = foundRecipe.getOutput(itemInput);
                        //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
                        if (!output.isZero() && energyContainer.insert(output, Action.SIMULATE, AutomationType.MANUAL).isZero()) {
                            //If we can accept it all, then add it and decrease our input
                            if (!energyContainer.insert(output, Action.EXECUTE, AutomationType.MANUAL).isZero()) {
                                MekanismUtils.logMismatchedStackSize();
                            }
                            int amountUsed = itemInput.getCount();
                            if (shrinkStack(amountUsed, Action.EXECUTE) != amountUsed) {
                                MekanismUtils.logMismatchedStackSize();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fills energy container from slot, does not try converting the item via any conversions conversion
     */
    public void fillContainer() {
        if (!isEmpty() && !energyContainer.getNeeded().isZero()) {
            //Try filling from the container's item
            fillContainerFromItem();
        }
    }

    /**
     * @implNote Does not pre-check if the current stack is empty or that the energy container needs any energy
     */
    private boolean fillContainerFromItem() {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        IStrictEnergyHandler itemHandler = EnergyCompatUtils.getStrictEnergyHandler(current);
        if (itemHandler != null) {
            boolean didTransfer = false;
            for (int container = 0; container < itemHandler.getEnergyContainerCount(); container++) {
                FloatingLong energyInItem = itemHandler.getEnergy(container);
                if (!energyInItem.isZero()) {
                    //Simulate inserting energy from each container in the item into our container
                    FloatingLong simulatedRemainder = energyContainer.insert(energyInItem, Action.SIMULATE, AutomationType.INTERNAL);
                    if (simulatedRemainder.smallerThan(energyInItem)) {
                        //If we were simulated that we could actually insert any, then
                        // extract up to as much energy as we were able to accept from the item
                        FloatingLong extractedEnergy = itemHandler.extractEnergy(container, energyInItem.subtract(simulatedRemainder), Action.EXECUTE);
                        if (!extractedEnergy.isZero()) {
                            //If we were able to actually extract it from the item, then insert it into our energy container
                            if (!energyContainer.insert(extractedEnergy, Action.EXECUTE, AutomationType.INTERNAL).isZero()) {
                                MekanismUtils.logMismatchedStackSize();
                            }
                            //and mark that we were able to transfer at least some of it
                            didTransfer = true;
                            if (energyContainer.getNeeded().isZero()) {
                                //If our energy container is full then exit early rather than continuing
                                // to check about filling the container from the item
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
     * Drains container into slot
     */
    public void drainContainer() {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        if (!isEmpty() && !energyContainer.isEmpty()) {
            IStrictEnergyHandler itemHandler = EnergyCompatUtils.getStrictEnergyHandler(current);
            if (itemHandler != null) {
                FloatingLong storedEnergy = energyContainer.getEnergy();
                FloatingLong simulatedRemainder = itemHandler.insertEnergy(storedEnergy, Action.SIMULATE);
                if (simulatedRemainder.smallerThan(storedEnergy)) {
                    //We are able to fit at least some of the energy from our container into the item
                    FloatingLong extractedEnergy = energyContainer.extract(storedEnergy.subtract(simulatedRemainder), Action.EXECUTE, AutomationType.INTERNAL);
                    if (!extractedEnergy.isZero()) {
                        //If we were able to actually extract it from our energy container, then insert it into the item
                        if (!itemHandler.insertEnergy(extractedEnergy, Action.EXECUTE).isZero()) {
                            MekanismUtils.logMismatchedStackSize();
                        }
                        onContentsChanged();
                    }
                }
            }
        }
    }
}