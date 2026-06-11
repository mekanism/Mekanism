package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.transaction.RateLimitTracker;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ChemicalInventorySlot extends ResourceHandlerSlot {

    public static boolean canFillOrConvert(IChemicalTank chemicalTank, Supplier<@Nullable Level> levelSupplier, ItemResource itemType) {
        if (canFill(chemicalTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.CHEMICAL.item())) {
            return true;
        }
        //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
        ItemStackToChemicalRecipe foundRecipe = MekanismRecipeType.CHEMICAL_CONVERSION.getInputCache().findTypeBasedRecipe(levelSupplier.get(), itemType);
        if (foundRecipe == null) {
            //No recipe, return that we can't insert it
            return false;
        }
        ChemicalResource conversion = ChemicalResource.of(foundRecipe.getOutput(itemType));
        //We allow insertion if the conversion isn't empty, and we can accept the resource type the conversion produces
        //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
        return !conversion.isEmpty() && simulateCanInsert(chemicalTank, conversion, AutomationType.MANUAL);
    }

    /// Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
    public static ChemicalInventorySlot rotary(IChemicalTank chemicalTank, BooleanSupplier isProcessingResource, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        Objects.requireNonNull(isProcessingResource, "The supplier that determines whether the resource is being processed cannot be null");
        return new ChemicalInventorySlot(chemicalTank, (itemType, automationType) -> !automationType.isExternal() || !canRotaryInsert(chemicalTank, itemType, Capabilities.CHEMICAL.item(), isProcessingResource),
              (itemType, automationType) -> automationType.isInternal() || canRotaryInsert(chemicalTank, itemType, Capabilities.CHEMICAL.item(), isProcessingResource), listener, x, y);
    }

    /// Fills the tank from this item
    public static ChemicalInventorySlot fill(IChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        return new ChemicalInventorySlot(chemicalTank, (itemType, automationType) -> !automationType.isExternal() || !canFill(chemicalTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.CHEMICAL.item()),
              (itemType, automationType) -> automationType.isInternal() || canFill(chemicalTank, ItemAccessUtils.sideEffectFreeAccess(itemType), Capabilities.CHEMICAL.item()), listener, x, y);
    }

    /// Accepts any items that can be filled with the current contents of the chemical tank, or if it is a chemical tank container and the tank is currently empty
    ///
    /// Drains the tank into this item.
    public static ChemicalInventorySlot drain(IChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        return new ChemicalInventorySlot(chemicalTank, (itemType, automationType) -> !automationType.isExternal() || !canDrain(chemicalTank, itemType, Capabilities.CHEMICAL.item()),
              (itemType, automationType) -> automationType.isInternal() || canDrain(chemicalTank, itemType, Capabilities.CHEMICAL.item()), listener, x, y);
    }

    /// Fills the tank from this item OR converts the given item to a gas
    public static ChemicalInventorySlot fillOrConvert(IChemicalTank gasTank, Supplier<@Nullable Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new ChemicalInventorySlot(gasTank, worldSupplier, (itemType, automationType) -> !automationType.isExternal() || !canFillOrConvert(gasTank, worldSupplier, itemType),
              (itemType, automationType) -> automationType.isInternal() || canFillOrConvert(gasTank, worldSupplier, itemType), null, null, listener, x, y);
    }

    private final Supplier<@Nullable Level> worldSupplier;
    protected final IChemicalTank chemicalTank;

    protected ChemicalInventorySlot(IChemicalTank chemicalTank, BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        this(chemicalTank, NO_LEVEL, canExtract, canInsert, null, null, listener, x, y);
    }

    protected ChemicalInventorySlot(IChemicalTank chemicalTank, Supplier<@Nullable Level> worldSupplier, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter,
          @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, insertionRateLimiter, extractionRateLimiter, listener, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.chemicalTank = chemicalTank;
        this.worldSupplier = worldSupplier;
    }

    /// Fills tank from slot, allowing for the item to also be converted to chemical if need be
    public void fillTankOrConvert(@Nullable TransactionContext transaction) {
        //Fill the tank from the item
        if (!fillTankFromSlot(transaction)) {
            //If filling from item failed, try doing it by conversion
            ItemStack current = resource().toStack(amountAsInt());
            ItemStackToChemicalRecipe foundRecipe = MekanismRecipeType.CHEMICAL_CONVERSION.getInputCache().findFirstRecipe(worldSupplier.get(), current);
            if (foundRecipe != null) {
                ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                if (!itemInput.isEmpty()) {
                    ChemicalStackTemplate output = foundRecipe.getOutput(itemInput);
                    try (Transaction subTransaction = Transaction.open(transaction)) {
                        int recipeNeeded = itemInput.count();
                        int chemicalProduced = output.amount();
                        //Try to extract the amount we need from our slot, and then insert the produced chemical into our tank
                        if (extract(ItemResource.of(itemInput), recipeNeeded, subTransaction, AutomationType.INTERNAL) == recipeNeeded &&
                            //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
                            chemicalTank.insert(ChemicalResource.of(output), chemicalProduced, subTransaction, AutomationType.MANUAL) == chemicalProduced) {
                            // if we succeeded, commit the changes
                            subTransaction.commit();
                        }
                    }
                }
            }
        }
    }

    /// Fills tank from slot, does not try converting the item via any conversions conversion
    public boolean fillTankFromSlot(@Nullable TransactionContext transaction) {
        return fillContainerFromSlot(chemicalTank, ContainerType.CHEMICAL, transaction);
    }

    /// Fills the container from the slot
    ///
    /// @param outputSlot The slot to move our container to after draining the item.
    public void fillTankFromSlot(IInventorySlot outputSlot, @Nullable TransactionContext transaction) {
        fillContainerFromSlot(chemicalTank, outputSlot, ContainerType.CHEMICAL, transaction);
    }

    /// Drains tank into slot
    public boolean drainTankIntoSlot(@Nullable TransactionContext transaction) {
        return drainContainerIntoSlot(chemicalTank, ContainerType.CHEMICAL, transaction);
    }

    /// Drains the container into the slot
    ///
    /// @param outputSlot The slot to move our container to after draining the resource container.
    public void drainTankIntoSlot(IInventorySlot outputSlot, @Nullable TransactionContext transaction) {
        drainContainerIntoSlot(chemicalTank, outputSlot, ContainerType.CHEMICAL, transaction);
    }
}