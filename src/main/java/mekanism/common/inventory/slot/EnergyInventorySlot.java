package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyInventorySlot extends BasicInventorySlot {

    public static final Predicate<ItemResource> HAS_ENERGY_HANDLER = itemType -> EnergyCompatUtils.getStrictEnergyHandler(ItemAccessUtils.queryOnlyAccess(itemType)) != null;

    /**
     * Gets the recipe for converting the given ItemResource into energy
     */
    @Nullable
    public static ItemStackToEnergyRecipe getPotentialConversion(@Nullable Level world, ItemResource itemType) {
        return MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findTypeBasedRecipe(world, itemType);
    }

    /**
     * Fills the container from this item OR converts the given item to energy
     */
    public static EnergyInventorySlot fillOrConvert(IEnergyContainer energyContainer, Supplier<@Nullable Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new EnergyInventorySlot(energyContainer, worldSupplier, (itemType, automationType) -> {
            if (!automationType.isExternal()) {
                //Always allow manual or internal extractions
                return true;
            }
            //Allow extraction if something went horribly wrong, and we are not an energy container item or no longer have any energy left to give,
            // or we are no longer a valid conversion, this might happen after a reload for example
            return !fillInsertCheck(itemType) && getPotentialConversion(worldSupplier.get(), itemType) == null;
        }, (itemType, automationType) -> {
            if (automationType.isInternal() || fillInsertCheck(itemType)) {
                return true;
            }
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            // Unlike with the chemical conversions, we don't check if the type is "valid" as we only have one "type" of energy.
            return getPotentialConversion(worldSupplier.get(), itemType) != null;
        }, itemType -> {
            //Note: we mark all energy handler items as valid and have a more restrictive insert check so that we allow full containers when they are done being filled
            // We also allow energy conversion of items that can be converted
            return EnergyCompatUtils.getStrictEnergyHandler(ItemAccessUtils.queryOnlyAccess(itemType)) != null || getPotentialConversion(worldSupplier.get(), itemType) != null;
        }, listener, x, y);
    }

    /**
     * Fills the container from this item
     */
    public static EnergyInventorySlot fill(IEnergyContainer energyContainer, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        return new EnergyInventorySlot(energyContainer, (itemType, automationType) -> !automationType.isExternal() || !fillInsertCheck(itemType),
              (itemType, automationType) -> automationType.isInternal() || fillInsertCheck(itemType), HAS_ENERGY_HANDLER, listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the energy container, or if it is an energy container and the container is currently empty
     * <p>
     * Drains the container into this item.
     */
    public static EnergyInventorySlot drain(IEnergyContainer energyContainer, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        return new EnergyInventorySlot(energyContainer, (itemType, automationType) -> !automationType.isExternal() || !drainInsertCheck(energyContainer, itemType),
              (itemType, automationType) -> automationType.isInternal() || drainInsertCheck(energyContainer, itemType), HAS_ENERGY_HANDLER, listener, x, y);
    }

    private static boolean drainInsertCheck(IEnergyContainer energyContainer, ItemResource itemType) {
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(ItemAccessUtils.queryOnlyAccess(itemType));
        if (itemEnergyHandler == null) {
            return false;
        }
        long storedEnergy = energyContainer.energy();
        if (storedEnergy == 0L) {
            //If the energy container is empty, accept the energy item as long as it is not full
            for (int container = 0; container < itemEnergyHandler.size(); container++) {
                if (itemEnergyHandler.getNeededEnergy(container) > 0L) {
                    //True if we have any space in this container
                    return true;
                }
            }
            return false;
        }
        //Otherwise, if we can accept any energy that is currently stored in the container, then we allow inserting the item
        try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
            return itemEnergyHandler.insert(storedEnergy, simulation) > 0;
        }
    }

    public static boolean fillInsertCheck(ItemResource itemType) {
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(ItemAccessUtils.queryOnlyAccess(itemType));
        //If we can extract any energy we are valid. Note: We can't just use FloatingLong.ONE as depending on conversion rates
        // that might be less than a single unit and thus can't be extracted
        if (itemEnergyHandler == null) {
            return false;
        }
        try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
            return itemEnergyHandler.extract(Long.MAX_VALUE, simulation) > 0L;
        }
    }

    private final Supplier<@Nullable Level> worldSupplier;
    private final IEnergyContainer energyContainer;

    private EnergyInventorySlot(IEnergyContainer energyContainer, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        this(energyContainer, () -> null, canExtract, canInsert, validator, listener, x, y);
    }

    private EnergyInventorySlot(IEnergyContainer energyContainer, Supplier<@Nullable Level> worldSupplier, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
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
                ItemStack current = resource().toStack(amountAsInt());
                ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findFirstRecipe(worldSupplier.get(), current);
                if (foundRecipe != null) {
                    ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                    if (!itemInput.isEmpty()) {
                        try (Transaction transaction = Transaction.openRoot()) {
                            int recipeNeeded = itemInput.count();
                            //Try to extract the amount we need from our slot
                            if (extract(ItemResource.of(itemInput), recipeNeeded, transaction, AutomationType.INTERNAL) == recipeNeeded) {
                                //If we succeeded, then try to insert the produced energy into our container
                                long output = foundRecipe.getOutput(itemInput);
                                //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
                                if (energyContainer.insert(output, transaction, AutomationType.MANUAL) == output) {
                                    // if we succeeded, commit the changes
                                    transaction.commit();
                                }
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
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(asItemAccess());
        if (itemEnergyHandler == null) {
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            long energyInItem;
            try (Transaction simulation = Transaction.open(transaction)) {
                //TODO - 26.1: Evaluate if we want to bother with this simulation or if there is a different way to do this
                energyInItem = itemEnergyHandler.extract(energyContainer.getNeeded(), simulation);
                if (energyInItem == 0) {
                    return false;
                }
            }
            //Simulate inserting energy from each container in the item into our container
            long inserted = energyContainer.insert(energyInItem, transaction, AutomationType.INTERNAL);
            if (inserted == 0) {
                //Nothing can be inserted into our container, exit
                return false;
            }
            //If we can actually insert any energy, then extract up to as much energy as we were able to accept from the item
            long extractedEnergy = itemEnergyHandler.extract(inserted, transaction);
            if (extractedEnergy == inserted) {
                //If we were able to actually extract it from the item, then insert it into our energy container
                transaction.commit();
                //and mark that we were able to transfer at least some of it
                //TODO - 26.1: I think the onContentsChanged should be handled by the item access and committing the transaction?
                //onContentsChanged();
                return true;
            }
            return false;
        }
    }

    /**
     * Drains container into slot
     */
    public void drainContainer() {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        if (isEmpty() || energyContainer.isEmpty()) {
            return;
        }
        IStrictEnergyHandler itemEnergyHandler = EnergyCompatUtils.getStrictEnergyHandler(asItemAccess());
        if (itemEnergyHandler == null) {
            return;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            long availableEnergy;
            try (Transaction simulation = Transaction.open(transaction)) {
                //TODO - 26.1: Evaluate if we want to bother with this simulation or if there is a different way to do this
                availableEnergy = energyContainer.extract(energyContainer.energy(), simulation, AutomationType.INTERNAL);
                if (availableEnergy == 0) {
                    //Short circuit, theoretically the item energy handler will do so as well, but we might as well ensure that it happens
                    return;
                }
            }
            //We are able to fit at least some energy from our container into the item
            long inserted = itemEnergyHandler.insert(availableEnergy, transaction);
            if (inserted > 0) {
                long extractedEnergy = energyContainer.extract(inserted, transaction, AutomationType.INTERNAL);
                if (extractedEnergy == inserted) {
                    //If we were able to actually extract it from our energy container, then commit all the changes
                    transaction.commit();
                    //TODO - 26.1: I think the onContentsChanged should be handled by the item access and committing the transaction?
                    //onContentsChanged();
                }
            }
        }
    }
}