package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.Mekanism;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyInventorySlot extends BasicInventorySlot {

    public static final Predicate<ItemStack> DRAIN_VALIDATOR = EnergyCompatUtils::hasStrictEnergyHandler;

    /**
     * Gets the energy from ItemStack conversion, ignoring the size of the item stack.
     */
    public static long getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
        ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findTypeBasedRecipe(world, itemStack);
        return foundRecipe == null ? 0L : foundRecipe.getOutput(itemStack);
    }

    /**
     * Fills the container from this item OR converts the given item to energy
     */
    public static EnergyInventorySlot fillOrConvert(IEnergyContainer energyContainer, Supplier<@Nullable Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new EnergyInventorySlot(energyContainer, worldSupplier, stack -> {
            //Allow extraction if something went horribly wrong, and we are not an energy container item or no longer have any energy left to give,
            // or we are no longer a valid conversion, this might happen after a reload for example
            return !fillInsertCheck(stack) && getPotentialConversion(worldSupplier.get(), stack) == 0L;
        }, stack -> {
            if (fillInsertCheck(stack)) {
                return true;
            }
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            // Unlike with the chemical conversions, we don't check if the type is "valid" as we only have one "type" of energy.
            return getPotentialConversion(worldSupplier.get(), stack) > 0L;
        }, stack -> {
            //Note: we mark all energy handler items as valid and have a more restrictive insert check so that we allow full containers when they are done being filled
            // We also allow energy conversion of items that can be converted
            return EnergyCompatUtils.hasStrictEnergyHandler(stack) || getPotentialConversion(worldSupplier.get(), stack) > 0L;
        }, listener, x, y);
    }

    /**
     * Fills the container from this item
     */
    public static EnergyInventorySlot fill(IEnergyContainer energyContainer, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        return new EnergyInventorySlot(energyContainer, stack -> !fillInsertCheck(stack), EnergyInventorySlot::fillInsertCheck,
              EnergyCompatUtils::hasStrictEnergyHandler, listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the energy container, or if it is an energy container and the container is currently empty
     * <p>
     * Drains the container into this item.
     */
    public static EnergyInventorySlot drain(IEnergyContainer energyContainer, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        Predicate<@NotNull ItemStack> insertPredicate = stack -> {
            IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(stack);
            if (itemEnergyHandler == null) {
                return false;
            }
            long storedEnergy = energyContainer.getEnergy();
            if (storedEnergy == 0L) {
                //If the energy container is empty, accept the energy item as long as it is not full
                for (int container = 0; container < itemEnergyHandler.getEnergyContainerCount(); container++) {
                    if (itemEnergyHandler.getNeededEnergy(container) > 0L) {
                        //True if we have any space in this container
                        return true;
                    }
                }
                return false;
            }
            //Otherwise, if we can accept any energy that is currently stored in the container, then we allow inserting the item
            return itemEnergyHandler.insertEnergy(storedEnergy, Action.SIMULATE) < storedEnergy;
        };
        return new EnergyInventorySlot(energyContainer, insertPredicate.negate(), insertPredicate, DRAIN_VALIDATOR, listener, x, y);
    }

    public static boolean fillInsertCheck(ItemStack stack) {
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(stack);
        //If we can extract any energy we are valid. Note: We can't just use FloatingLong.ONE as depending on conversion rates
        // that might be less than a single unit and thus can't be extracted
        return itemEnergyHandler != null && itemEnergyHandler.extractEnergy(Long.MAX_VALUE, Action.SIMULATE) > 0L;
    }

    private final Supplier<@Nullable Level> worldSupplier;
    private final IEnergyContainer energyContainer;

    private EnergyInventorySlot(IEnergyContainer energyContainer, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert,
          Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        this(energyContainer, () -> null, canExtract, canInsert, validator, listener, x, y);
    }

    private EnergyInventorySlot(IEnergyContainer energyContainer, Supplier<@Nullable Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
        this.energyContainer = energyContainer;
        this.worldSupplier = worldSupplier;
        setSlotType(ContainerSlotType.POWER);
        setSlotOverlay(SlotOverlay.POWER);
    }

    /**
     * Fills the energy container from slot, allowing for the item to also be converted to energy if need be (example redstone -> energy)
     */
    public void fillContainerOrConvert() {
        if (!isEmpty() && energyContainer.getNeeded() > 0L) {
            //Fill the container from the item
            if (!fillContainerFromItem()) {
                //If filling from item failed, try doing it by conversion
                ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findFirstRecipe(worldSupplier.get(), current);
                if (foundRecipe != null) {
                    ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                    if (!itemInput.isEmpty()) {
                        long output = foundRecipe.getOutput(itemInput);
                        //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
                        if (energyContainer.insert(output, Action.SIMULATE, AutomationType.MANUAL) == 0L) {
                            //If we can accept it all, then add it and decrease our input
                            MekanismUtils.logExpectedZero(energyContainer.insert(output, Action.EXECUTE, AutomationType.MANUAL));
                            int amountUsed = itemInput.getCount();
                            MekanismUtils.logMismatchedStackSize(shrinkStack(amountUsed, Action.EXECUTE), amountUsed);
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
        if (!isEmpty() && energyContainer.getNeeded() > 0L) {
            //Try filling from the container's item
            fillContainerFromItem();
        }
    }

    /**
     * @implNote Does not pre-check if the current stack is empty or that the energy container needs any energy
     */
    private boolean fillContainerFromItem() {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(current);
        if (itemEnergyHandler != null) {
            long energyInItem = itemEnergyHandler.extractEnergy(energyContainer.getNeeded(), Action.SIMULATE);
            if (energyInItem > 0L) {
                //Simulate inserting energy from each container in the item into our container
                long simulatedRemainder = energyContainer.insert(energyInItem, Action.SIMULATE, AutomationType.INTERNAL);
                if (simulatedRemainder < energyInItem) {
                    //If we were simulated that we could actually insert any, then
                    // extract up to as much energy as we were able to accept from the item
                    long toPull = energyInItem - simulatedRemainder;
                    simulatedRemainder = energyContainer.insert(toPull, Action.SIMULATE, AutomationType.INTERNAL);
                    if (simulatedRemainder == 0L) {
                        long extractedEnergy = itemEnergyHandler.extractEnergy(toPull, Action.EXECUTE);
                        if (extractedEnergy > 0L) {
                            //If we were able to actually extract it from the item, then insert it into our energy container
                            MekanismUtils.logExpectedZero(energyContainer.insert(extractedEnergy, Action.EXECUTE, AutomationType.INTERNAL));
                            //and mark that we were able to transfer at least some of it
                            onContentsChanged();
                            return true;
                        }
                    } else {
                        Mekanism.logger.error("EnergyInventorySlot#fillContainerFromItem: Simulation after extraction calculation had a remainder. Tried pulling {}, remainder {}", toPull, simulatedRemainder);
                    }
                }
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
            IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(current);
            if (itemEnergyHandler != null) {
                long storedEnergy = energyContainer.getEnergy();
                long simulatedRemainder = itemEnergyHandler.insertEnergy(storedEnergy, Action.SIMULATE);
                if (simulatedRemainder < storedEnergy) {
                    //We are able to fit at least some energy from our container into the item
                    long toOffer = storedEnergy - simulatedRemainder;
                    simulatedRemainder = itemEnergyHandler.insertEnergy(toOffer, Action.SIMULATE);
                    if (simulatedRemainder == 0L) {
                        long extractedEnergy = energyContainer.extract(toOffer, Action.EXECUTE, AutomationType.INTERNAL);
                        if (extractedEnergy > 0L) {
                            //If we were able to actually extract it from our energy container, then insert it into the item
                            MekanismUtils.logExpectedZero(itemEnergyHandler.insertEnergy(extractedEnergy, Action.EXECUTE));
                            onContentsChanged();
                        }
                    } else {
                        Mekanism.logger.error("EnergyInventorySlot#drainContainer: Simulation after insertion calculation had a remainder. Offered {}, remainder {}", toOffer, simulatedRemainder);
                    }
                }
            }
        }
    }
}