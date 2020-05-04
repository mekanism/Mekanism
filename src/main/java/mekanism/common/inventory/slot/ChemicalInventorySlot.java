package mekanism.common.inventory.slot;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.datafixers.util.Pair;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalInventorySlot<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends BasicInventorySlot {

    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<@NonNull ItemStack> getFillOrConvertExtractPredicate(
          IChemicalTank<CHEMICAL, STACK> chemicalTank, Function<@NonNull ItemStack, IChemicalHandlerWrapper<CHEMICAL, STACK>> wrapperFunction,
          Function<ItemStack, STACK> potentialConversionSupplier) {
        return stack -> {
            IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper = wrapperFunction.apply(stack);
            if (wrapper != null) {
                for (int tank = 0; tank < wrapper.getTanks(); tank++) {
                    if (chemicalTank.isValid(wrapper.getChemicalInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //Only allow extraction if our item is out of chemical, and doesn't have a valid conversion for it
            }
            //Always allow extraction if something went horribly wrong and we are not a chemical item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            STACK conversion = potentialConversionSupplier.apply(stack);
            return conversion.isEmpty() || !chemicalTank.isValid(conversion);
        };
    }

    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<@NonNull ItemStack> getFillOrConvertInsertPredicate(
          IChemicalTank<CHEMICAL, STACK> chemicalTank, Function<@NonNull ItemStack, IChemicalHandlerWrapper<CHEMICAL, STACK>> wrapperFunction,
          Function<ItemStack, STACK> potentialConversionSupplier) {
        return stack -> {
            if (fillInsertCheck(chemicalTank, wrapperFunction.apply(stack))) {
                return true;
            }
            STACK conversion = potentialConversionSupplier.apply(stack);
            //Note: We recheck about this being empty and that it is still valid as the conversion list might have changed, such as after a reload
            return !conversion.isEmpty() && chemicalTank.insert(conversion, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < conversion.getAmount();
        };
    }

    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<@NonNull ItemStack> getFillExtractPredicate(
          IChemicalTank<CHEMICAL, STACK> chemicalTank, Function<@NonNull ItemStack, IChemicalHandlerWrapper<CHEMICAL, STACK>> wrapperFunction) {
        return stack -> {
            IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper = wrapperFunction.apply(stack);
            if (wrapper != null) {
                for (int tank = 0; tank < wrapper.getTanks(); tank++) {
                    if (chemicalTank.isValid(wrapper.getChemicalInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //If we have no contents that are still valid, allow extraction
            }
            //Always allow it if something went horribly wrong and we are not a chemical item
            return true;
        };
    }

    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean fillInsertCheck(IChemicalTank<CHEMICAL, STACK> chemicalTank,
          @Nullable IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper) {
        if (wrapper != null) {
            for (int tank = 0; tank < wrapper.getTanks(); tank++) {
                STACK chemicalInTank = wrapper.getChemicalInTank(tank);
                if (!chemicalInTank.isEmpty() && chemicalTank.insert(chemicalInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < chemicalInTank.getAmount()) {
                    //True if we can fill the tank with any of our contents
                    // Note: We need to recheck the fact the chemical is not empty in case the item has multiple tanks and only some of the chemicals are valid
                    return true;
                }
            }
        }
        return false;
    }

    protected static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> Predicate<@NonNull ItemStack> getDrainInsertPredicate(
          IChemicalTank<CHEMICAL, STACK> chemicalTank, Function<@NonNull ItemStack, IChemicalHandlerWrapper<CHEMICAL, STACK>> wrapperFunction) {
        return stack -> {
            IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper = wrapperFunction.apply(stack);
            if (wrapper != null) {
                if (chemicalTank.isEmpty()) {
                    //If the chemical tank is empty, accept the chemical item as long as it is not full
                    for (int tank = 0; tank < wrapper.getTanks(); tank++) {
                        if (wrapper.getChemicalInTank(tank).getAmount() < wrapper.getTankCapacity(tank)) {
                            //True if we have any space in this tank
                            return true;
                        }
                    }
                    return false;
                }
                //Otherwise if we can accept any of the chemical that is currently stored in the tank, then we allow inserting the item
                return wrapper.insertChemical(chemicalTank.getStack(), Action.SIMULATE).getAmount() < chemicalTank.getStored();
            }
            return false;
        };
    }

    protected final Supplier<World> worldSupplier;
    protected final IChemicalTank<CHEMICAL, STACK> chemicalTank;

    protected ChemicalInventorySlot(IChemicalTank<CHEMICAL, STACK> chemicalTank, Supplier<World> worldSupplier, Predicate<@NonNull ItemStack> canExtract,
          Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        setSlotType(ContainerSlotType.EXTRA);
        this.chemicalTank = chemicalTank;
        this.worldSupplier = worldSupplier;
    }

    @Nullable
    protected abstract IChemicalHandlerWrapper<CHEMICAL, STACK> getCapabilityWrapper();

    @Nullable
    protected abstract Pair<ItemStack, STACK> getConversion();

    /**
     * Fills tank from slot, allowing for the item to also be converted to chemical if need be
     */
    public void fillTankOrConvert() {
        if (!isEmpty() && chemicalTank.getNeeded() > 0) {
            //Fill the tank from the item
            if (!fillTankFromItem()) {
                //If filling from item failed, try doing it by conversion
                Pair<ItemStack, STACK> conversion = getConversion();
                if (conversion != null) {
                    STACK output = conversion.getSecond();
                    //Note: We use manual as the automation type to bypass our container's rate limit insertion checks
                    if (!output.isEmpty() && chemicalTank.insert(output, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
                        //If we can accept it all, then add it and decrease our input
                        MekanismUtils.logMismatchedStackSize(chemicalTank.insert(output, Action.EXECUTE, AutomationType.MANUAL).getAmount(), 0);
                        int amountUsed = conversion.getFirst().getCount();
                        MekanismUtils.logMismatchedStackSize(shrinkStack(amountUsed, Action.EXECUTE), amountUsed);
                    }
                }
            }
        }
    }

    /**
     * Fills tank from slot, does not try converting the item via any conversions conversion
     */
    public void fillTank() {
        fillChemicalTank(this, chemicalTank, getCapabilityWrapper());
    }

    public boolean fillTankFromItem() {
        return fillChemicalTankFromItem(this, chemicalTank, getCapabilityWrapper());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void fillChemicalTank(IInventorySlot slot,
          IChemicalTank<CHEMICAL, STACK> chemicalTank, IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper) {
        if (!slot.isEmpty() && chemicalTank.getNeeded() > 0) {
            //Try filling from the tank's item
            fillChemicalTankFromItem(slot, chemicalTank, wrapper);
        }
    }

    /**
     * @implNote Does not pre-check if the current stack is empty or that the chemical tank needs chemical
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean fillChemicalTankFromItem(IInventorySlot slot,
          IChemicalTank<CHEMICAL, STACK> chemicalTank, IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper) {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's chemical items stack so at the moment it doesn't fully matter
        if (wrapper != null) {
            boolean didTransfer = false;
            for (int tank = 0; tank < wrapper.getTanks(); tank++) {
                STACK chemicalInItem = wrapper.getChemicalInTank(tank);
                if (!chemicalInItem.isEmpty()) {
                    //Simulate inserting chemical from each tank in the item into our tank
                    STACK simulatedRemainder = chemicalTank.insert(chemicalInItem, Action.SIMULATE, AutomationType.INTERNAL);
                    long chemicalInItemAmount = chemicalInItem.getAmount();
                    long remainder = simulatedRemainder.getAmount();
                    if (remainder < chemicalInItemAmount) {
                        //If we were simulated that we could actually insert any, then
                        // extract up to as much chemical as we were able to accept from the item
                        STACK extractedChemical = wrapper.extractChemical(tank, chemicalInItemAmount - remainder, Action.EXECUTE);
                        if (!extractedChemical.isEmpty()) {
                            //If we were able to actually extract it from the item, then insert it into our chemical tank
                            MekanismUtils.logMismatchedStackSize(chemicalTank.insert(extractedChemical, Action.EXECUTE, AutomationType.INTERNAL).getAmount(), 0);
                            //and mark that we were able to transfer at least some of it
                            didTransfer = true;
                            if (chemicalTank.getNeeded() == 0) {
                                //If our tank is full then exit early rather than continuing
                                // to check about filling the tank from the item
                                break;
                            }
                        }
                    }
                }
            }
            if (didTransfer) {
                slot.onContentsChanged();
                return true;
            }
        }
        return false;
    }

    public void drainTank() {
        drainChemicalTank(this, chemicalTank, getCapabilityWrapper());
    }

    /**
     * Drains tank into slot
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void drainChemicalTank(IInventorySlot slot, IChemicalTank<CHEMICAL, STACK> chemicalTank,
          IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper) {
        //TODO: Do we need to/want to add any special handling for if the handler is stacked? For example with how buckets are for fluids
        // Note: None of Mekanism's chemical items stack so at the moment it doesn't fully matter
        if (!slot.isEmpty() && !chemicalTank.isEmpty()) {
            if (wrapper != null) {
                STACK storedChemical = chemicalTank.getStack();
                STACK simulatedRemainder = wrapper.insertChemical(storedChemical, Action.SIMULATE);
                long remainder = simulatedRemainder.getAmount();
                long amount = storedChemical.getAmount();
                if (remainder < amount) {
                    //We are able to fit at least some of the chemical from our tank into the item
                    STACK extractedChemical = chemicalTank.extract(amount - remainder, Action.EXECUTE, AutomationType.INTERNAL);
                    if (!extractedChemical.isEmpty()) {
                        //If we were able to actually extract it from our tank, then insert it into the item
                        MekanismUtils.logMismatchedStackSize(wrapper.insertChemical(extractedChemical, Action.EXECUTE).getAmount(), 0);
                        slot.onContentsChanged();
                    }
                }
            }
        }
    }
}