package mekanism.common.inventory.slot.chemical;

import com.google.common.primitives.Ints;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.ResourceUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault//TODO - 26.1: Make most of the methods between this and IFluidHandlerSlot use generics so that we don't have to implement them twice
public class ChemicalInventorySlot extends BasicInventorySlot {

    /**
     * Gets the ChemicalStack from ItemResource conversion.
     */
    public static ChemicalStack getPotentialConversion(@Nullable Level world, ItemResource itemType) {
        ItemStackToChemicalRecipe foundRecipe = MekanismRecipeType.CHEMICAL_CONVERSION.getInputCache().findTypeBasedRecipe(world, itemType);
        return foundRecipe == null ? ChemicalStack.EMPTY : foundRecipe.getOutput(itemType);
    }

    public static boolean fillOrConvertExtractCheck(IChemicalTank chemicalTank, Supplier<Level> levelSupplier, ItemResource itemType) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemType);
        if (handler != null) {
            for (int tank = 0, size = handler.size(); tank < size; tank++) {
                ChemicalResource stored = handler.getResource(tank);
                //TODO - 26.1: This didn't used to check if it is empty, was there a reason for that?
                if (!stored.isEmpty() && chemicalTank.isValid(stored)) {
                    //False if the items contents are still valid
                    return false;
                }
            }
            //Only allow extraction if our item is out of chemical, and doesn't have a valid conversion for it
        }
        //Always allow extraction if something went horribly wrong, and we are not a chemical item AND we can't provide a valid type of chemical
        // This might happen after a reload for example
        ChemicalStack conversion = getPotentialConversion(levelSupplier.get(), itemType);
        return conversion.isEmpty() || !chemicalTank.isValid(ChemicalResource.of(conversion));
    }

    public static boolean fillOrConvertInsertCheck(IChemicalTank chemicalTank, Supplier<Level> levelSupplier, ItemResource itemType) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemType);
        if (handler != null && fillInsertCheck(chemicalTank, handler)) {
            return true;
        }
        ChemicalStack conversion = getPotentialConversion(levelSupplier.get(), itemType);
        //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
        if (conversion.isEmpty()) {
            return false;
        }
        ChemicalResource conversionType = ChemicalResource.of(conversion);
        //TODO - 26.1: Re-evaluate this clamping
        if (simulateCanInsert(chemicalTank, conversionType, Ints.saturatedCast(conversion.amount()))) {
            //If we can insert the converted substance into the tank allow insertion
            return true;
        }
        //If we can't because the tank is full, we do a slightly less accurate check and validate that the type matches the stored type
        // and that it is still actually valid for the tank, as a reload could theoretically make it no longer be valid while there is still some stored
        return chemicalTank.getNeededAsLong() == 0 && chemicalTank.getResource().equals(conversionType) && chemicalTank.isValid(conversionType);
    }

    public static Predicate<ItemResource> getFillExtractPredicate(IChemicalTank chemicalTank) {
        return itemType -> {
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemType);
            return handler == null || fillExtractCheck(chemicalTank, handler);
        };
    }

    public static boolean fillExtractCheck(IChemicalTank chemicalTank, ResourceHandler<ChemicalResource> handler) {
        //TODO - 26.1: Extract this check from the fill or convert predicate and here into a helper method
        for (int tank = 0, size = handler.size(); tank < size; tank++) {
            ChemicalResource storedChemical = handler.getResource(tank);
            if (!storedChemical.isEmpty() && chemicalTank.isValid(storedChemical)) {
                //False if the item isn't empty and the contents are still valid
                return false;
            }
        }
        //If we have no contents that are still valid, allow extraction
        return true;
    }

    public static boolean fillInsertCheck(IChemicalTank chemicalTank, ItemResource itemType) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemType);
        return handler != null && fillInsertCheck(chemicalTank, handler);
    }

    public static boolean fillInsertCheck(IChemicalTank chemicalTank, ResourceHandler<ChemicalResource> handler) {
        for (int tank = 0, size = handler.size(); tank < size; tank++) {
            ChemicalResource chemicalType = handler.getResource(tank);
            if (!chemicalType.isEmpty() && simulateCanInsert(chemicalTank, chemicalType, handler.getAmountAsInt(tank))) {
                //True if we can fill the tank with any of our contents
                return true;
            }
        }
        return false;
    }

    public static Predicate<ItemResource> getDrainInsertPredicate(IChemicalTank chemicalTank) {
        //TODO - 26.1: Re-evaluate this method, and if we want to inline to canFill anywhere
        //TODO - 26.1: Figure out item access
        return itemType -> canDrainInsert(chemicalTank, ItemAccess.forStack(itemType.toStack()));
    }

    public static boolean canDrainInsert(IChemicalTank chemicalTank, ItemAccess itemAccess) {
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        return handler != null && canDrainInsert(chemicalTank, handler);
    }

    public static boolean canDrainInsert(IChemicalTank chemicalTank, ResourceHandler<ChemicalResource> handler) {
        if (chemicalTank.isEmpty()) {
            //If the chemical tank is empty, accept the chemical item as long as it is not full
            //TODO - 26.1: Should we make this also have the chemical type have to match a desired type???
            return !ResourceHandlerUtil.isFull(handler);
        }
        //TODO - 26.1: Are our insert predicates and stuff ever ran from within a transactional context?
        // If so we might need to pass Transaction#getCurrentOpenedTransaction to it
        try (Transaction simulation = Transaction.openRoot()) {
            //Otherwise, if we can accept any of the chemical that is currently stored in the tank, then we allow inserting the item
            return handler.insert(chemicalTank.getResource(), chemicalTank.amount(), simulation) > 0;
        }
    }

    /**
     * Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static ChemicalInventorySlot rotaryDrain(IChemicalTank chemicalTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        Predicate<ItemResource> drainInsertPredicate = getDrainInsertPredicate(chemicalTank);
        Predicate<ItemResource> insertPredicate = itemType -> modeSupplier.getAsBoolean() && drainInsertPredicate.test(itemType);
        return new ChemicalInventorySlot(chemicalTank, insertPredicate.negate(), insertPredicate, listener, x, y);
    }

    /**
     * Fills the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static ChemicalInventorySlot rotaryFill(IChemicalTank chemicalTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        return new ChemicalInventorySlot(chemicalTank, getFillExtractPredicate(chemicalTank),
              itemType -> !modeSupplier.getAsBoolean() && fillInsertCheck(chemicalTank, itemType), listener, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static ChemicalInventorySlot fill(IChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        return new ChemicalInventorySlot(chemicalTank, getFillExtractPredicate(chemicalTank), itemType -> fillInsertCheck(chemicalTank, itemType), listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the chemical tank, or if it is a chemical tank container and the tank is currently empty
     * <p>
     * Drains the tank into this item.
     */
    public static ChemicalInventorySlot drain(IChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        Predicate<@NotNull ItemResource> insertPredicate = getDrainInsertPredicate(chemicalTank);
        return new ChemicalInventorySlot(chemicalTank, insertPredicate.negate(), insertPredicate, listener, x, y);
    }

    //TODO - 26.1: Make a helper that uses generics for this and fluid inventory slot rather than having it be duplicated
    private static boolean simulateCanInsert(IChemicalTank chemicalTank, ChemicalResource chemicalType, int amount) {
        //TODO - 26.1: Are call sites ever in a transactional context?
        /*try (Transaction simulation = Transaction.openRoot()) {
            return chemicalTank.insert(chemicalType, amount, simulation, AutomationType.INTERNAL) > 0;
        }*/
        //TODO - 26.1: This used to do a full on simulation, do we need to check to make sure it isn't full or is not checking it actually more accurate for what we want
        // If so we can easily check that it isn't full if the resource type matches, or we might want to go back to simulation,
        // even though that means we mightneed to be careful about the transactional context
        if (chemicalTank.isValidForInsertion(chemicalType, AutomationType.INTERNAL)) {
            //Calculate if the fluid is ever valid for insertion into the fluid tank
            //If it is and our tank is currently empty or has the same type of resource
            // that means the items contents are valid, and we can fill the tank with any of our contents
            return chemicalTank.isEmpty() || chemicalTank.getResource().equals(chemicalType);
        }
        return false;
    }

    private final Supplier<Level> worldSupplier;
    protected final IChemicalTank chemicalTank;

    protected ChemicalInventorySlot(IChemicalTank gasTank, Predicate<ItemResource> canExtract, Predicate<ItemResource> canInsert, @Nullable IContentsListener listener,
          int x, int y) {
        this(gasTank, () -> null, canExtract, canInsert, listener, x, y);
    }

    protected ChemicalInventorySlot(IChemicalTank chemicalTank, Supplier<Level> worldSupplier, Predicate<ItemResource> canExtract,
          Predicate<ItemResource> canInsert, @Nullable IContentsListener listener, int x, int y) {
        this(chemicalTank, worldSupplier, canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
        //Note: We pass alwaysTrue as the validator, so that if a mod only exposes a chemical handler when an item isn't stacked
        // then we don't crash and burn when it is stacked
        //TODO: Eventually maybe we want to somehow enforce what the max stack size is for a given item and mark it as able to be accepted
        // but only a single one of it so that we can provide the short circuit "is ever valid" check to mods querying our item handlers
        // but at least for now given we fail fast, it shouldn't be *that* big a deal
        // Similarly, this also means we don't currently allow inserting stacked items, which is probably correct, though if something tries to
        // insert it stacked, and it would have a capability and be valid if they tried with only one item, we don't accept it
        // (instead of only accepting a single item). This is the potentially more important reason why to address this comment
    }

    protected ChemicalInventorySlot(IChemicalTank chemicalTank, Supplier<Level> worldSupplier, Predicate<ItemResource> canExtract,
          Predicate<ItemResource> canInsert, Predicate<ItemResource> validator, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.chemicalTank = chemicalTank;
        this.worldSupplier = worldSupplier;
    }

    /**
     * Fills the tank from this item OR converts the given item to a gas
     */
    public static ChemicalInventorySlot fillOrConvert(IChemicalTank gasTank, Supplier<Level> worldSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(worldSupplier, "World supplier cannot be null");
        return new ChemicalInventorySlot(gasTank, worldSupplier, itemType -> fillOrConvertExtractCheck(gasTank, worldSupplier, itemType),
              itemType -> fillOrConvertInsertCheck(gasTank, worldSupplier, itemType), listener, x, y);
    }

    /**
     * Fills tank from slot, allowing for the item to also be converted to chemical if need be
     */
    public void fillTankOrConvert() {
        //Fill the tank from the item
        if (!fillTank(this, chemicalTank, itemAccess())) {
            //If filling from item failed, try doing it by conversion
            ItemStack current = getResource().toStack(amount());
            ItemStackToChemicalRecipe foundRecipe = MekanismRecipeType.CHEMICAL_CONVERSION.getInputCache().findFirstRecipe(worldSupplier.get(), current);
            if (foundRecipe != null) {
                ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                if (!itemInput.isEmpty()) {
                    ChemicalStack output = foundRecipe.getOutput(itemInput);
                    if (!output.isEmpty()) {
                        try (Transaction transaction = Transaction.openRoot()) {
                            int recipeNeeded = itemInput.count();
                            //TODO - 26.1: Make chemical stacks just be ints?
                            int chemicalProduced = Ints.saturatedCast(output.amount());
                            //Try to extract the amount we need from our slot, and then insert the produced chemical into our tank
                            if (extract(ItemResource.of(itemInput), recipeNeeded, transaction, AutomationType.INTERNAL) == recipeNeeded &&
                                //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
                                chemicalTank.insert(ChemicalResource.of(output), chemicalProduced, transaction, AutomationType.MANUAL) == chemicalProduced) {
                                // if we succeeded, commit the changes
                                transaction.commit();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fills tank from slot, does not try converting the item via any conversions conversion
     */
    public void fillTank() {
        //Try filling from the tank's item
        fillTank(this, chemicalTank, itemAccess());
    }

    public static boolean fillTank(IInventorySlot slot, IChemicalTank chemicalTank, ItemAccess itemAccess) {
        if (slot.isEmpty() || chemicalTank.getNeeded() == 0) {
            return false;
        }
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's chemical items stack so at the moment it doesn't fully matter
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
        if (handler != null) {
            ChemicalResource typeToExtract = ResourceUtils.getTypeToExtract(chemicalTank, handler, AutomationType.INTERNAL, null);
            if (typeToExtract.isEmpty()) {
                //No known type, try to see what valid types we are able to extract from the handler
                return false;
            }
            int amountToExtract;
            try (Transaction simulation = Transaction.openRoot()) {
                amountToExtract = handler.extract(typeToExtract, chemicalTank.getNeeded(), simulation);
                if (amountToExtract == 0) {
                    return false;
                }
            }
            try (Transaction transaction = Transaction.openRoot()) {
                //Insert the chemical from each tank in the item into our tank
                int inserted = chemicalTank.insert(typeToExtract, amountToExtract, transaction, AutomationType.INTERNAL);
                //If we can insert any, and can extract up to as much chemical as we were able to accept from the item
                if (inserted > 0 && handler.extract(typeToExtract, inserted, transaction) == inserted) {
                    //Commit the transfer
                    transaction.commit();
                    //and mark that we were able to transfer at least some of it
                    slot.onContentsChanged();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Drains tank into slot
     */
    public void drainTank() {
        drainTank(this, chemicalTank, itemAccess());
    }

    public static void drainTank(IInventorySlot slot, IChemicalTank chemicalTank, ItemAccess itemAccess) {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's chemical items stack so at the moment it doesn't fully matter
        if (!slot.isEmpty() && !chemicalTank.isEmpty()) {
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(itemAccess);
            if (handler != null) {
                ChemicalResource chemicalType = chemicalTank.getResource();
                //TODO - 26.1: Do we need to simulate how much we can actually drain? In case there is an extraction rate from the tank
                int amountToTransfer = chemicalTank.amount();
                try (Transaction transaction = Transaction.openRoot()) {
                    int inserted = handler.insert(chemicalType, amountToTransfer, transaction);
                    //We are able to fit at least some of the chemical from our tank into the item
                    // and can drain that amount from our tank
                    if (inserted > 0 && chemicalTank.extract(chemicalType, inserted, transaction, AutomationType.INTERNAL) == inserted) {
                        //If we were able to actually extract it from our tank, then insert it into the item
                        transaction.commit();
                        slot.onContentsChanged();
                    }
                }
            }
        }
    }
}