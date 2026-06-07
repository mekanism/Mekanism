package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.ItemAccessUtils;
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

    /**
     * Fills the container from this item OR converts the given item to energy
     */
    public static EnergyInventorySlot fillOrConvert(IEnergyContainer energyContainer, Supplier<@Nullable Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new EnergyInventorySlot(energyContainer, worldSupplier, (itemType, automationType) -> !automationType.isExternal() || !canFillOrConvert(energyContainer, worldSupplier, itemType),
              (itemType, automationType) -> automationType.isInternal() || canFillOrConvert(energyContainer, worldSupplier, itemType), null, null, listener, x, y);
    }

    /**
     * Fills the container from this item
     */
    public static EnergyInventorySlot fill(IEnergyContainer energyContainer, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        return new EnergyInventorySlot(energyContainer, (itemType, automationType) -> !automationType.isExternal() || !canFill(energyContainer, itemType),
              (itemType, automationType) -> automationType.isInternal() || canFill(energyContainer, itemType), listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the energy container, or if it is an energy container and the container is currently empty
     * <p>
     * Drains the container into this item.
     */
    public static EnergyInventorySlot drain(IEnergyContainer energyContainer, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(energyContainer, "Energy container cannot be null");
        return new EnergyInventorySlot(energyContainer, (itemType, automationType) -> !automationType.isExternal() || !canDrain(energyContainer, itemType),
              (itemType, automationType) -> automationType.isInternal() || canDrain(energyContainer, itemType), listener, x, y);
    }

    private static boolean canDrain(EnergyHandler energyContainer, ItemResource itemType) {
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccessUtils.sideEffectFreeAccess(itemType));
        if (energyHandler == null) {
            return false;
        }
        return canDrain(energyContainer, energyHandler);
    }

    public static boolean canDrain(EnergyHandler storage, EnergyHandler energyHandler) {
        if (storage.getAmountAsLong() == 0) {
            //If the energy container is empty, accept the energy item as long as it is not full
            return energyHandler.getAmountAsLong() < energyHandler.getCapacityAsLong();
        }
        IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(storage);
        if (energyContainer != null && !energyContainer.isValidForExtraction(AutomationType.INTERNAL)) {
            return false;
        }
        try (Transaction simulation = TransactionHelper.openTransactionSafe()) {
            //Otherwise, if we can accept any energy that is currently stored in the container, then we allow inserting the item
            //Note: We try to insert the max amount we can store, in case the energy handler is like a bucket and can only accept
            // amounts in specific increments
            return energyHandler.insert(storage.getCapacityAsInt(), simulation) > 0;
        }
    }

    public static boolean canFillOrConvert(@Nullable IEnergyContainer energyContainer, Supplier<@Nullable Level> levelSupplier, ItemResource itemType) {
        if (canFill(energyContainer, itemType)) {
            return true;
        }
        //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
        ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findTypeBasedRecipe(levelSupplier.get(), itemType);
        if (foundRecipe == null) {
            //No recipe, return that we can't insert it
            return false;
        }
        //If we don't know enough information about our energy handler, or we can insert into it manually
        // consider it a conversion we can accept
        return energyContainer == null || energyContainer.isValidForInsertion(AutomationType.MANUAL);
    }

    public static boolean canFill(@Nullable IEnergyContainer energyContainer, ItemResource itemType) {
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(ItemAccessUtils.sideEffectFreeAccess(itemType));
        if (energyHandler == null) {
            return false;
        } else if (energyContainer != null && !energyContainer.isValidForExtraction(AutomationType.INTERNAL)) {
            return false;
        }
        try (Transaction simulation = TransactionHelper.openTransactionSafe()) {
            //If we can extract any energy we are valid
            return energyHandler.extract(Integer.MAX_VALUE, simulation) > 0;
        }
    }

    private final Supplier<@Nullable Level> worldSupplier;
    private final IEnergyContainer energyContainer;

    private EnergyInventorySlot(IEnergyContainer energyContainer, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        this(energyContainer, NO_LEVEL, canExtract, canInsert, null, null, listener, x, y);
    }

    private EnergyInventorySlot(IEnergyContainer energyContainer, Supplier<@Nullable Level> worldSupplier, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter,
          @Nullable IContentsListener listener, int x, int y) {
        //Note: We pass alwaysTrue as the validator, so that if a mod only exposes a resource handler on the filled item or when the item isn't stacked
        // then we don't have it all of a sudden being invalid after it is emptied
        super(canExtract, canInsert, ConstantPredicates.alwaysTrue(), insertionRateLimiter, extractionRateLimiter, listener, x, y);
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
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(asItemAccess());
        if (energyHandler == null) {
            return false;
        }
        int roomFor;
        try (Transaction simulation = Transaction.open(transaction)) {
            //Check how much we can actually insert into our container in case it has a rate limit and can't accept everything it needs at once
            roomFor = energyContainer.insert(energyContainer.getNeededAsInt(), simulation, AutomationType.INTERNAL);
            if (roomFor == 0) {
                return false;
            }
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            //Extract the amount we simulated we can accept from the handler. It is important this happens before we then insert into our rate limit
            // based container as if the handler a stacked item, then it might only be able to provide things in discrete increments
            int extracted = energyHandler.extract(roomFor, subTransaction);
            if (extracted > 0 && energyContainer.insert(extracted, subTransaction, AutomationType.INTERNAL) == extracted) {
                //If we were able to accept  something, and extract the corresponding amount from the original handler
                //Commit the changes to the transaction
                subTransaction.commit();
                return true;
            }
            return false;
        }
    }

    /**
     * Drains container into slot
     */
    public void drainContainerIntoSlot(@Nullable TransactionContext transaction) {
        if (isEmpty() || energyContainer.isEmpty()) {
            return;
        }
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(asItemAccess());
        if (energyHandler == null) {
            return;
        }
        int availableEnergy;
        try (Transaction simulation = Transaction.open(transaction)) {
            //Check how much we can extract from the container to ensure we follow any transfer rate limits
            availableEnergy = energyContainer.extract(energyContainer.getAmountAsInt(), simulation, AutomationType.INTERNAL);
            if (availableEnergy == 0) {
                //Short circuit, theoretically the item energy handler will do so as well, but we might as well ensure that it happens
                return;
            }
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            //We are able to fit at least some energy from our container into the item
            int inserted = energyHandler.insert(availableEnergy, subTransaction);
            if (inserted > 0 && energyContainer.extract(inserted, subTransaction, AutomationType.INTERNAL) == inserted) {
                //If we were able to actually extract it from our energy container, then commit all the changes
                subTransaction.commit();
            }
        }
    }
}