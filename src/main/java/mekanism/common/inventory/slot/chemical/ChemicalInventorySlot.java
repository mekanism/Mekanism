package mekanism.common.inventory.slot.chemical;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalInventorySlot<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends BasicInventorySlot {

    /**
     * Gets the ChemicalStack from ItemStack conversion, ignoring the size of the item stack.
     */
    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>>
    STACK getPotentialConversion(IMekanismRecipeTypeProvider<?, RECIPE, SingleItem<RECIPE>> recipeType, @Nullable Level world, ItemStack itemStack, STACK empty) {
        ItemStackToChemicalRecipe<CHEMICAL, STACK> foundRecipe = recipeType.getInputCache().findTypeBasedRecipe(world, itemStack);
        return foundRecipe == null ? empty : foundRecipe.getOutput(itemStack);
    }

    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<@NotNull ItemStack> getFillOrConvertExtractPredicate(
          IChemicalTank<CHEMICAL, STACK> chemicalTank, MultiTypeCapability<? extends IChemicalHandler<CHEMICAL, STACK>> chemicalCapability,
          Function<ItemStack, STACK> potentialConversionSupplier) {
        return stack -> {
            IChemicalHandler<CHEMICAL, STACK> handler = chemicalCapability.getCapability(stack);
            if (handler != null) {
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    if (chemicalTank.isValid(handler.getChemicalInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //Only allow extraction if our item is out of chemical, and doesn't have a valid conversion for it
            }
            //Always allow extraction if something went horribly wrong, and we are not a chemical item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            STACK conversion = potentialConversionSupplier.apply(stack);
            return conversion.isEmpty() || !chemicalTank.isValid(conversion);
        };
    }

    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<@NotNull ItemStack> getFillOrConvertInsertPredicate(
          IChemicalTank<CHEMICAL, STACK> chemicalTank, MultiTypeCapability<? extends IChemicalHandler<CHEMICAL, STACK>> chemicalCapability,
          Function<ItemStack, STACK> potentialConversionSupplier) {
        return stack -> {
            if (fillInsertCheck(chemicalTank, chemicalCapability, stack)) {
                return true;
            }
            STACK conversion = potentialConversionSupplier.apply(stack);
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

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<@NotNull ItemStack> getFillExtractPredicate(
          IChemicalTank<CHEMICAL, STACK> chemicalTank, MultiTypeCapability<? extends IChemicalHandler<CHEMICAL, STACK>> chemicalCapability) {
        return stack -> {
            IChemicalHandler<CHEMICAL, STACK> handler = chemicalCapability.getCapability(stack);
            if (handler != null) {
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    STACK storedChemical = handler.getChemicalInTank(tank);
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

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean fillInsertCheck(IChemicalTank<CHEMICAL, STACK> chemicalTank,
          MultiTypeCapability<? extends IChemicalHandler<CHEMICAL, STACK>> chemicalCapability, ItemStack stack) {
        IChemicalHandler<CHEMICAL, STACK> handler = chemicalCapability.getCapability(stack);
        if (handler != null) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                STACK chemicalInTank = handler.getChemicalInTank(tank);
                if (!chemicalInTank.isEmpty() && chemicalTank.insert(chemicalInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < chemicalInTank.getAmount()) {
                    //True if we can fill the tank with any of our contents
                    // Note: We need to recheck the fact the chemical is not empty in case the item has multiple tanks and only some of the chemicals are valid
                    return true;
                }
            }
        }
        return false;
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<@NotNull ItemStack> getDrainInsertPredicate(
          IChemicalTank<CHEMICAL, STACK> chemicalTank, MultiTypeCapability<? extends IChemicalHandler<CHEMICAL, STACK>> chemicalCapability) {
        return stack -> {
            IChemicalHandler<CHEMICAL, STACK> handler = chemicalCapability.getCapability(stack);
            if (handler != null) {
                if (chemicalTank.isEmpty()) {
                    //If the chemical tank is empty, accept the chemical item as long as it is not full
                    for (int tank = 0; tank < handler.getTanks(); tank++) {
                        if (handler.getChemicalInTank(tank).getAmount() < handler.getTankCapacity(tank)) {
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

    protected final Supplier<Level> worldSupplier;
    protected final IChemicalTank<CHEMICAL, STACK> chemicalTank;

    protected ChemicalInventorySlot(IChemicalTank<CHEMICAL, STACK> chemicalTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        this(chemicalTank, worldSupplier, canExtract, canInsert, alwaysTrue, listener, x, y);
        //Note: We pass alwaysTrue as the validator, so that if a mod only exposes a chemical handler when an item isn't stacked
        // then we don't crash and burn when it is stacked
        //TODO: Eventually maybe we want to somehow enforce what the max stack size is for a given item and mark it as able to be accepted
        // but only a single one of it so that we can provide the short circuit "is ever valid" check to mods querying our item handlers
        // but at least for now given we fail fast, it shouldn't be *that* big a deal
        // Similarly, this also means we don't currently allow inserting stacked items, which is probably correct, though if something tries to
        // insert it stacked, and it would have a capability and be valid if they tried with only one item, we don't accept it
        // (instead of only accepting a single item). This is the potentially more important reason why to address this comment
    }

    protected ChemicalInventorySlot(IChemicalTank<CHEMICAL, STACK> chemicalTank, Supplier<Level> worldSupplier, Predicate<@NotNull ItemStack> canExtract,
          Predicate<@NotNull ItemStack> canInsert, Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.chemicalTank = chemicalTank;
        this.worldSupplier = worldSupplier;
    }

    @Nullable
    protected IChemicalHandler<CHEMICAL, STACK> getCapability() {
        return getChemicalCapability().getCapability(current);
    }

    protected abstract MultiTypeCapability<? extends IChemicalHandler<CHEMICAL, STACK>> getChemicalCapability();

    @Nullable
    protected ItemStackToChemicalRecipe<CHEMICAL, STACK> getConversionRecipe(@Nullable Level world, ItemStack stack) {
        return null;
    }

    /**
     * Fills tank from slot, allowing for the item to also be converted to chemical if need be
     */
    public void fillTankOrConvert() {
        if (!isEmpty() && chemicalTank.getNeeded() > 0) {
            //Fill the tank from the item
            if (!fillChemicalTankFromItem(this, chemicalTank, getCapability())) {
                //If filling from item failed, try doing it by conversion
                ItemStackToChemicalRecipe<CHEMICAL, STACK> foundRecipe = getConversionRecipe(worldSupplier.get(), current);
                if (foundRecipe != null) {
                    ItemStack itemInput = foundRecipe.getInput().getMatchingInstance(current);
                    if (!itemInput.isEmpty()) {
                        STACK output = foundRecipe.getOutput(itemInput);
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

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void fillChemicalTank(IInventorySlot slot,
          IChemicalTank<CHEMICAL, STACK> chemicalTank, @Nullable IChemicalHandler<CHEMICAL, STACK> handler) {
        if (!slot.isEmpty() && chemicalTank.getNeeded() > 0) {
            //Try filling from the tank's item
            fillChemicalTankFromItem(slot, chemicalTank, handler);
        }
    }

    /**
     * @implNote Does not pre-check if the current stack is empty or that the chemical tank needs chemical
     */
    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean fillChemicalTankFromItem(IInventorySlot slot,
          IChemicalTank<CHEMICAL, STACK> chemicalTank, @Nullable IChemicalHandler<CHEMICAL, STACK> handler) {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's chemical items stack so at the moment it doesn't fully matter
        if (handler != null) {
            STACK toExtract = chemicalTank.isEmpty() ?
                                   //No known type, just extract to the tank's capacity of any type
                                   handler.extractChemical(chemicalTank.getCapacity(), Action.SIMULATE) :
                                   //We have a known type in the tank, try to extract the amount we need to fill the tank, using that type of chemical
                                   handler.extractChemical(ChemicalUtil.copyWithAmount(chemicalTank.getStack(), chemicalTank.getNeeded()), Action.SIMULATE);
            if (!toExtract.isEmpty()) {
                //Simulate inserting chemical from each tank in the item into our tank
                STACK simulatedRemainder = chemicalTank.insert(toExtract, Action.SIMULATE, AutomationType.INTERNAL);
                toExtract.shrink(simulatedRemainder.getAmount());
                if (!toExtract.isEmpty()) {
                    //If we were simulated that we could actually insert any, then
                    // extract up to as much chemical as we were able to accept from the item
                    STACK extractedChemical = handler.extractChemical(toExtract, Action.EXECUTE);
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
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void drainChemicalTank(IInventorySlot slot,
          IChemicalTank<CHEMICAL, STACK> chemicalTank, @Nullable IChemicalHandler<CHEMICAL, STACK> handler) {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's chemical items stack so at the moment it doesn't fully matter
        if (!slot.isEmpty() && !chemicalTank.isEmpty() && handler != null) {
            STACK storedChemical = chemicalTank.getStack();
            STACK simulatedRemainder = handler.insertChemical(storedChemical, Action.SIMULATE);
            long remainder = simulatedRemainder.getAmount();
            long amount = storedChemical.getAmount();
            if (remainder < amount) {
                //We are able to fit at least some of the chemical from our tank into the item
                STACK extractedChemical = chemicalTank.extract(amount - remainder, Action.EXECUTE, AutomationType.INTERNAL);
                if (!extractedChemical.isEmpty()) {
                    //If we were able to actually extract it from our tank, then insert it into the item
                    MekanismUtils.logMismatchedStackSize(handler.insertChemical(extractedChemical, Action.EXECUTE).getAmount(), 0);
                    slot.onContentsChanged();
                }
            }
        }
    }
}