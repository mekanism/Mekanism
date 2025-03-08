package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalInventorySlot extends BasicInventorySlot {

    /**
     * Gets the ChemicalStack from ItemStack conversion, ignoring the size of the item stack.
     */
    public static ChemicalStack getPotentialConversion(@Nullable Level world, ItemStack itemStack) {
        ItemStackToChemicalRecipe foundRecipe = MekanismRecipeType.CHEMICAL_CONVERSION.getInputCache().findTypeBasedRecipe(world, itemStack);
        return foundRecipe == null ? ChemicalStack.EMPTY : foundRecipe.getOutput(itemStack);
    }

    private static Predicate<@NotNull ItemStack> getFillOrConvertExtractPredicate(IChemicalTank chemicalTank, Supplier<Level> levelSupplier) {
        return stack -> {
            IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
            if (handler != null) {
                for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                    if (chemicalTank.isValid(handler.getChemicalInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //Only allow extraction if our item is out of chemical, and doesn't have a valid conversion for it
            }
            //Always allow extraction if something went horribly wrong, and we are not a chemical item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            ChemicalStack conversion = getPotentialConversion(levelSupplier.get(), stack);
            return conversion.isEmpty() || !chemicalTank.isValid(conversion);
        };
    }

    private static Predicate<@NotNull ItemStack> getFillOrConvertInsertPredicate(IChemicalTank chemicalTank, Supplier<Level> levelSupplier) {
        return stack -> {
            if (fillInsertCheck(chemicalTank, stack)) {
                return true;
            }
            ChemicalStack conversion = getPotentialConversion(levelSupplier.get(), stack);
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            if (conversion.isEmpty()) {
                return false;
            }
            if (chemicalTank.insert(conversion, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < conversion.getAmount()) {
                //If we can insert the converted substance into the tank allow insertion
                return true;
            }
            //If we can't because the tank is full, we do a slightly less accurate check and validate that the type matches the stored type
            // and that it is still actually valid for the tank, as a reload could theoretically make it no longer be valid while there is still some stored
            return chemicalTank.getNeeded() == 0 && chemicalTank.isTypeEqual(conversion) && chemicalTank.isValid(conversion);
        };
    }

    public static Predicate<@NotNull ItemStack> getFillExtractPredicate(IChemicalTank chemicalTank) {
        return stack -> {
            IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
            if (handler != null) {
                for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                    ChemicalStack storedChemical = handler.getChemicalInTank(tank);
                    if (!storedChemical.isEmpty() && chemicalTank.isValid(storedChemical)) {
                        //False if the item isn't empty and the contents are still valid
                        return false;
                    }
                }
                //If we have no contents that are still valid, allow extraction
            }
            //Always allow it if we are not a chemical item (For example this may be true for hybrid inventory slots)
            return true;
        };
    }

    public static boolean fillInsertCheck(IChemicalTank chemicalTank, ItemStack stack) {
        IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
        if (handler != null) {
            for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                ChemicalStack chemicalInTank = handler.getChemicalInTank(tank);
                if (!chemicalInTank.isEmpty() && chemicalTank.insert(chemicalInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < chemicalInTank.getAmount()) {
                    //True if we can fill the tank with any of our contents
                    // Note: We need to recheck the fact the chemical is not empty in case the item has multiple tanks and only some of the chemicals are valid
                    return true;
                }
            }
        }
        return false;
    }

    public static Predicate<@NotNull ItemStack> getDrainInsertPredicate(IChemicalTank chemicalTank) {
        return stack -> {
            IChemicalHandler handler = Capabilities.CHEMICAL.getCapability(stack);
            if (handler != null) {
                if (chemicalTank.isEmpty()) {
                    //If the chemical tank is empty, accept the chemical item as long as it is not full
                    for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
                        if (handler.getChemicalInTank(tank).getAmount() < handler.getChemicalTankCapacity(tank)) {
                            //True if we have any space in this tank
                            return true;
                        }
                    }
                    return false;
                }
                //Otherwise, if we can accept any of the chemical that is currently stored in the tank, then we allow inserting the item
                return handler.insertChemical(chemicalTank.getStack(), Action.SIMULATE).getAmount() < chemicalTank.getStored();
            }
            return false;
        };
    }

    /**
     * Drains the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static ChemicalInventorySlot rotaryDrain(IChemicalTank chemicalTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        Predicate<@NotNull ItemStack> drainInsertPredicate = getDrainInsertPredicate(chemicalTank);
        Predicate<@NotNull ItemStack> insertPredicate = stack -> modeSupplier.getAsBoolean() && drainInsertPredicate.test(stack);
        return new ChemicalInventorySlot(chemicalTank, insertPredicate.negate(), insertPredicate, listener, x, y);
    }

    /**
     * Fills the tank depending on if this item has any contents in it AND if the supplied boolean's mode supports it
     */
    public static ChemicalInventorySlot rotaryFill(IChemicalTank chemicalTank, BooleanSupplier modeSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        Objects.requireNonNull(modeSupplier, "Mode supplier cannot be null");
        return new ChemicalInventorySlot(chemicalTank, getFillExtractPredicate(chemicalTank),
              stack -> !modeSupplier.getAsBoolean() && fillInsertCheck(chemicalTank, stack), listener, x, y);
    }

    /**
     * Fills the tank from this item
     */
    public static ChemicalInventorySlot fill(IChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        return new ChemicalInventorySlot(chemicalTank, getFillExtractPredicate(chemicalTank), stack -> fillInsertCheck(chemicalTank, stack), listener, x, y);
    }

    /**
     * Accepts any items that can be filled with the current contents of the chemical tank, or if it is a chemical tank container and the tank is currently empty
     * <p>
     * Drains the tank into this item.
     */
    public static ChemicalInventorySlot drain(IChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Chemical tank cannot be null");
        Predicate<@NotNull ItemStack> insertPredicate = getDrainInsertPredicate(chemicalTank);
        return new ChemicalInventorySlot(chemicalTank, insertPredicate.negate(), insertPredicate, listener, x, y);
    }

    private final Supplier<Level> worldSupplier;
    protected final IChemicalTank chemicalTank;

    protected ChemicalInventorySlot(IChemicalTank gasTank, Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener,
          int x, int y) {
        this(gasTank, () -> null, canExtract, canInsert, listener, x, y);
    }

    protected ChemicalInventorySlot(IChemicalTank chemicalTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
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

    protected ChemicalInventorySlot(IChemicalTank chemicalTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
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
        return new ChemicalInventorySlot(gasTank, worldSupplier, getFillOrConvertExtractPredicate(gasTank, worldSupplier),
              getFillOrConvertInsertPredicate(gasTank, worldSupplier), listener, x, y);
    }

    @Nullable
    protected IChemicalHandler getCapability() {
        return Capabilities.CHEMICAL.getCapability(current);
    }

    /**
     * Fills tank from slot, allowing for the item to also be converted to chemical if need be
     */
    public void fillTankOrConvert() {
        if (!isEmpty() && chemicalTank.getNeeded() > 0) {
            //Fill the tank from the item
            if (!fillChemicalTankFromItem(this, chemicalTank, getCapability())) {
                //If filling from item failed, try doing it by conversion
                ItemStackToChemicalRecipe foundRecipe = MekanismRecipeType.CHEMICAL_CONVERSION.getInputCache().findFirstRecipe(worldSupplier.get(), current);
                if (foundRecipe != null) {
                    ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                    if (!itemInput.isEmpty()) {
                        ChemicalStack output = foundRecipe.getOutput(itemInput);
                        //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
                        if (!output.isEmpty() && chemicalTank.insert(output, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
                            //If we can accept it all, then add it and decrease our input
                            MekanismUtils.logMismatchedStackSize(chemicalTank.insert(output, Action.EXECUTE, AutomationType.MANUAL).getAmount(), 0);
                            int amountUsed = itemInput.getCount();
                            MekanismUtils.logMismatchedStackSize(shrinkStack(amountUsed, Action.EXECUTE), amountUsed);
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
        fillChemicalTank(this, chemicalTank, getCapability());
    }

    public static void fillChemicalTank(IInventorySlot slot, IChemicalTank chemicalTank, @Nullable IChemicalHandler handler) {
        if (!slot.isEmpty() && chemicalTank.getNeeded() > 0) {
            //Try filling from the tank's item
            fillChemicalTankFromItem(slot, chemicalTank, handler);
        }
    }

    /**
     * @implNote Does not pre-check if the current stack is empty or that the chemical tank needs chemical
     */
    private static boolean fillChemicalTankFromItem(IInventorySlot slot, IChemicalTank chemicalTank, @Nullable IChemicalHandler handler) {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's chemical items stack so at the moment it doesn't fully matter
        if (handler != null) {
            ChemicalStack toExtract;//No known type, just extract to the tank's capacity of any type
            // We have a known type in the tank, try to extract the amount we need to fill the tank, using that type of chemical
            if (chemicalTank.isEmpty()) {
                toExtract = handler.extractChemical(chemicalTank.getCapacity(), Action.SIMULATE);
            } else {
                ChemicalStack stack = chemicalTank.getStack();
                long amount = chemicalTank.getNeeded();
                toExtract = handler.extractChemical(stack.copyWithAmount(amount), Action.SIMULATE);
            }
            if (!toExtract.isEmpty()) {
                //Simulate inserting chemical from each tank in the item into our tank
                ChemicalStack simulatedRemainder = chemicalTank.insert(toExtract, Action.SIMULATE, AutomationType.INTERNAL);
                toExtract.shrink(simulatedRemainder.getAmount());
                if (!toExtract.isEmpty()) {
                    //If we were simulated that we could actually insert any, then
                    // extract up to as much chemical as we were able to accept from the item
                    ChemicalStack extractedChemical = handler.extractChemical(toExtract, Action.EXECUTE);
                    if (!extractedChemical.isEmpty()) {
                        //If we were able to actually extract it from the item, then insert it into our chemical tank
                        MekanismUtils.logMismatchedStackSize(chemicalTank.insert(extractedChemical, Action.EXECUTE, AutomationType.INTERNAL).getAmount(), 0);
                        //and mark that we were able to transfer at least some of it
                        slot.onContentsChanged();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void drainTank() {
        drainChemicalTank(this, chemicalTank, getCapability());
    }

    /**
     * Drains tank into slot
     */
    public static void drainChemicalTank(IInventorySlot slot, IChemicalTank chemicalTank, @Nullable IChemicalHandler handler) {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's chemical items stack so at the moment it doesn't fully matter
        if (!slot.isEmpty() && !chemicalTank.isEmpty() && handler != null) {
            ChemicalStack storedChemical = chemicalTank.getStack();
            ChemicalStack simulatedRemainder = handler.insertChemical(storedChemical, Action.SIMULATE);
            long remainder = simulatedRemainder.getAmount();
            long amount = storedChemical.getAmount();
            if (remainder < amount) {
                //We are able to fit at least some of the chemical from our tank into the item
                ChemicalStack extractedChemical = chemicalTank.extract(amount - remainder, Action.EXECUTE, AutomationType.INTERNAL);
                if (!extractedChemical.isEmpty()) {
                    //If we were able to actually extract it from our tank, then insert it into the item
                    MekanismUtils.logMismatchedStackSize(handler.insertChemical(extractedChemical, Action.EXECUTE).getAmount(), 0);
                    slot.onContentsChanged();
                }
            }
        }
    }
}