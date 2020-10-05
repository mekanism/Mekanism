package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.slot.chemical.InfusionInventorySlot;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.inventory.slot.chemical.PigmentInventorySlot;
import mekanism.common.inventory.slot.chemical.SlurryInventorySlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class HybridInventorySlot extends MergedChemicalInventorySlot<MergedTank> implements IFluidHandlerSlot {

    private static boolean hasCapability(@Nonnull ItemStack stack) {
        return FluidUtil.getFluidHandler(stack).isPresent() || stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent() ||
               stack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY).isPresent() || stack.getCapability(Capabilities.PIGMENT_HANDLER_CAPABILITY).isPresent() ||
               stack.getCapability(Capabilities.SLURRY_HANDLER_CAPABILITY).isPresent();
    }

    public static HybridInventorySlot inputOrDrain(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        Predicate<@NonNull ItemStack> fluidInsertPredicate = FluidInventorySlot.getInputPredicate(mergedTank.getFluidTank());
        Predicate<@NonNull ItemStack> gasInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(mergedTank.getGasTank(), GasInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> infusionInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(mergedTank.getInfusionTank(), InfusionInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> pigmentInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(mergedTank.getPigmentTank(), PigmentInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> slurryInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(mergedTank.getSlurryTank(), SlurryInventorySlot::getCapability);
        BiPredicate<@NonNull ItemStack, @NonNull AutomationType> insertPredicate = (stack, automationType) -> {
            CurrentType currentType = mergedTank.getCurrentType();
            if (currentType == CurrentType.FLUID) {
                return fluidInsertPredicate.test(stack);
            } else if (currentType == CurrentType.GAS) {
                return gasInsertPredicate.test(stack);
            } else if (currentType == CurrentType.INFUSION) {
                return infusionInsertPredicate.test(stack);
            } else if (currentType == CurrentType.PIGMENT) {
                return pigmentInsertPredicate.test(stack);
            } else if (currentType == CurrentType.SLURRY) {
                return slurryInsertPredicate.test(stack);
            }//Else the tank is empty, check if any insert predicate is valid
            return fluidInsertPredicate.test(stack) || gasInsertPredicate.test(stack) || infusionInsertPredicate.test(stack) || pigmentInsertPredicate.test(stack) ||
                   slurryInsertPredicate.test(stack);
        };
        //Extract predicate, always allow the player to manually extract or if the insert predicate no longer matches allow for it to be extracted
        return new HybridInventorySlot(mergedTank, (stack, automationType) -> automationType == AutomationType.MANUAL || !insertPredicate.test(stack, automationType),
              insertPredicate, HybridInventorySlot::hasCapability, listener, x, y);
    }

    public static HybridInventorySlot outputOrFill(MergedTank mergedTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        Predicate<@NonNull ItemStack> gasExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(mergedTank.getGasTank(), GasInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> infusionExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(mergedTank.getInfusionTank(), InfusionInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> pigmentExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(mergedTank.getPigmentTank(), PigmentInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> slurryExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(mergedTank.getSlurryTank(), SlurryInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> gasInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(mergedTank.getGasTank(), GasInventorySlot.getCapability(stack));
        Predicate<@NonNull ItemStack> infusionInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(mergedTank.getInfusionTank(), InfusionInventorySlot.getCapability(stack));
        Predicate<@NonNull ItemStack> pigmentInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(mergedTank.getPigmentTank(), PigmentInventorySlot.getCapability(stack));
        Predicate<@NonNull ItemStack> slurryInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(mergedTank.getSlurryTank(), SlurryInventorySlot.getCapability(stack));
        return new HybridInventorySlot(mergedTank, (stack, automationType) -> {
            if (automationType == AutomationType.MANUAL) {
                //Always allow the player to manually extract
                return true;
            }
            CurrentType currentType = mergedTank.getCurrentType();
            if (currentType == CurrentType.FLUID) {
                //Always allow extracting from a "fluid output" slot
                return true;
            } else if (currentType == CurrentType.GAS) {
                return gasExtractPredicate.test(stack);
            } else if (currentType == CurrentType.INFUSION) {
                return infusionExtractPredicate.test(stack);
            } else if (currentType == CurrentType.PIGMENT) {
                return pigmentExtractPredicate.test(stack);
            } else if (currentType == CurrentType.SLURRY) {
                return slurryExtractPredicate.test(stack);
            }//Else the tank is empty, check all our extraction predicates
            return gasExtractPredicate.test(stack) && infusionExtractPredicate.test(stack) && pigmentExtractPredicate.test(stack) && slurryExtractPredicate.test(stack);
        }, (stack, automationType) -> {
            CurrentType currentType = mergedTank.getCurrentType();
            if (currentType == CurrentType.FLUID) {
                //Only allow inserting internally for "fluid output" slots
                return automationType == AutomationType.INTERNAL;
            } else if (currentType == CurrentType.GAS) {
                return gasInsertPredicate.test(stack);
            } else if (currentType == CurrentType.INFUSION) {
                return infusionInsertPredicate.test(stack);
            } else if (currentType == CurrentType.PIGMENT) {
                return pigmentInsertPredicate.test(stack);
            } else if (currentType == CurrentType.SLURRY) {
                return slurryInsertPredicate.test(stack);
            }//Else the tank is empty, if the item is a fluid handler and it is an internal check allow it
            if (automationType == AutomationType.INTERNAL && FluidUtil.getFluidHandler(stack).isPresent()) {
                return true;
            }
            //otherwise only allow it if one of the chemical insert predicates matches
            return gasInsertPredicate.test(stack) || infusionInsertPredicate.test(stack) || pigmentInsertPredicate.test(stack) || slurryInsertPredicate.test(stack);
        }, HybridInventorySlot::hasCapability, listener, x, y);
    }

    // used by IFluidHandlerSlot
    private boolean isDraining;
    private boolean isFilling;

    private HybridInventorySlot(MergedTank mergedTank, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(mergedTank, canExtract, canInsert, validator, listener, x, y);
    }

    @Override
    public IExtendedFluidTank getFluidTank() {
        return mergedTank.getFluidTank();
    }

    @Override
    public boolean isDraining() {
        return isDraining;
    }

    @Override
    public boolean isFilling() {
        return isFilling;
    }

    @Override
    public void setDraining(boolean draining) {
        isDraining = draining;
    }

    @Override
    public void setFilling(boolean filling) {
        isFilling = filling;
    }
}
