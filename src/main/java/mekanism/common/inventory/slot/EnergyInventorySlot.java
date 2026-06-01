package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class EnergyInventorySlot extends BasicInventorySlot {

    public static final Predicate<ItemResource> HAS_ENERGY_HANDLER = itemType -> Capabilities.ENERGY.getCapability(ItemAccessUtils.queryOnlyAccess(itemType)) != null;

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
            return HAS_ENERGY_HANDLER.test(itemType) || getPotentialConversion(worldSupplier.get(), itemType) != null;
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

    private static boolean drainInsertCheck(EnergyHandler energyContainer, ItemResource itemType) {
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        if (energyHandler == null) {
            return false;
        }
        return drainInsertCheck(energyContainer, energyHandler);
    }

    public static boolean drainInsertCheck(EnergyHandler energyContainer, EnergyHandler energyHandler) {
        int storedEnergy = energyContainer.getAmountAsInt();
        if (storedEnergy == 0) {
            //If the energy container is empty, accept the energy item as long as it is not full
            return energyHandler.getAmountAsLong() < energyHandler.getCapacityAsLong();
        }
        //Otherwise, if we can accept any energy that is currently stored in the container, then we allow inserting the item
        try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
            return energyHandler.insert(storedEnergy, simulation) > 0;
        }
    }

    public static boolean fillInsertCheck(ItemResource itemType) {
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        //If we can extract any energy we are valid. Note: We can't just use FloatingLong.ONE as depending on conversion rates
        // that might be less than a single unit and thus can't be extracted
        if (energyHandler == null) {
            return false;
        }
        try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
            return energyHandler.extract(Integer.MAX_VALUE, simulation) > 0;
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
    public void fillContainerOrConvert(@Nullable TransactionContext transaction) {
        //Fill the container from the item
        if (!fillContainerFromSlot(transaction)) {
            //If filling from item failed, try doing it by conversion
            ItemStack current = resource().toStack(amountAsInt());
            ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findFirstRecipe(worldSupplier.get(), current);
            if (foundRecipe != null) {
                ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                if (!itemInput.isEmpty()) {
                    try (Transaction subTransaction = Transaction.open(transaction)) {
                        int recipeNeeded = itemInput.count();
                        //Try to extract the amount we need from our slot
                        if (extract(ItemResource.of(itemInput), recipeNeeded, subTransaction, AutomationType.INTERNAL) == recipeNeeded) {
                            //If we succeeded, then try to insert the produced energy into our container
                            int output = foundRecipe.getOutput(itemInput);
                            //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
                            if (energyContainer.insert(output, subTransaction, AutomationType.MANUAL) == output) {
                                // if we succeeded, commit the changes
                                subTransaction.commit();
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
    public boolean fillContainerFromSlot(@Nullable TransactionContext transaction) {
        if (isEmpty() || EnergyHandlerUtil.isFull(energyContainer)) {
            return false;
        }
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(asItemAccess());
        if (energyHandler == null) {
            return false;
        }
        int energyInItem;
        try (Transaction simulation = Transaction.open(transaction)) {
            //TODO - 26.1: Evaluate if we want to bother with this simulation or if there is a different way to do this
            energyInItem = energyHandler.extract(energyContainer.getNeededAsInt(), simulation);
            if (energyInItem == 0) {
                return false;
            }
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            //Simulate inserting energy from each container in the item into our container
            int inserted = energyContainer.insert(energyInItem, subTransaction, AutomationType.INTERNAL);
            if (inserted > 0 && energyHandler.extract(inserted, subTransaction) == inserted) {
                //If we can actually insert any energy, then extract up to as much energy as we were able to accept from the item
                //If we were able to actually extract it from the item, then commit the changes
                subTransaction.commit();
                //and mark that we were able to transfer at least some of it
                return true;
            }
            return false;
        }
    }

    /**
     * Drains container into slot
     */
    public void drainContainerIntoSlot(@Nullable TransactionContext transaction) {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        if (isEmpty() || energyContainer.isEmpty()) {
            return;
        }
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(asItemAccess());
        if (energyHandler == null) {
            return;
        }
        int availableEnergy;
        try (Transaction simulation = Transaction.open(transaction)) {
            //TODO - 26.1: Evaluate if we want to bother with this simulation or if there is a different way to do this
            availableEnergy = energyContainer.extract(energyContainer.getAmountAsInt(), simulation, AutomationType.INTERNAL);
            if (availableEnergy == 0) {
                //Short circuit, theoretically the item energy handler will do so as well, but we might as well ensure that it happens
                return;
            }
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            //We are able to fit at least some energy from our container into the item
            int inserted = energyHandler.insert(availableEnergy, subTransaction);
            if (inserted > 0) {
                long extractedEnergy = energyContainer.extract(inserted, subTransaction, AutomationType.INTERNAL);
                if (extractedEnergy == inserted) {
                    //If we were able to actually extract it from our energy container, then commit all the changes
                    subTransaction.commit();
                }
            }
        }
    }
}