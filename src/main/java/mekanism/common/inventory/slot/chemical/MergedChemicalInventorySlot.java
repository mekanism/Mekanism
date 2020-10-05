package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.item.ItemStack;

public class MergedChemicalInventorySlot<MERGED extends MergedChemicalTank> extends BasicInventorySlot {

    private static boolean hasCapability(@Nonnull ItemStack stack) {
        return stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent() || stack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY).isPresent() ||
               stack.getCapability(Capabilities.PIGMENT_HANDLER_CAPABILITY).isPresent() || stack.getCapability(Capabilities.SLURRY_HANDLER_CAPABILITY).isPresent();
    }

    public static MergedChemicalInventorySlot<MergedChemicalTank> drain(MergedChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Merged chemical tank cannot be null");
        Predicate<@NonNull ItemStack> gasInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getGasTank(), GasInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> infusionInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getInfusionTank(), InfusionInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> pigmentInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getPigmentTank(), PigmentInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> slurryInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getSlurryTank(), SlurryInventorySlot::getCapability);
        BiPredicate<@NonNull ItemStack, @NonNull AutomationType> insertPredicate = (stack, automationType) -> {
            Current current = chemicalTank.getCurrent();
            if (current == Current.GAS) {
                return gasInsertPredicate.test(stack);
            } else if (current == Current.INFUSION) {
                return infusionInsertPredicate.test(stack);
            } else if (current == Current.PIGMENT) {
                return pigmentInsertPredicate.test(stack);
            } else if (current == Current.SLURRY) {
                return slurryInsertPredicate.test(stack);
            }//Else the tank is empty, check if any insert predicate is valid
            return gasInsertPredicate.test(stack) || infusionInsertPredicate.test(stack) || pigmentInsertPredicate.test(stack) || slurryInsertPredicate.test(stack);
        };
        //Extract predicate, always allow the player to manually extract or if the insert predicate no longer matches allow for it to be extracted
        return new MergedChemicalInventorySlot<>(chemicalTank, (stack, automationType) -> automationType == AutomationType.MANUAL || !insertPredicate.test(stack, automationType),
              insertPredicate, MergedChemicalInventorySlot::hasCapability, listener, x, y);
    }

    public static MergedChemicalInventorySlot<MergedChemicalTank> fill(MergedChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Merged chemical tank cannot be null");
        Predicate<@NonNull ItemStack> gasExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getGasTank(), GasInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> infusionExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getInfusionTank(), InfusionInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> pigmentExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getPigmentTank(), PigmentInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> slurryExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getSlurryTank(), SlurryInventorySlot::getCapability);
        Predicate<@NonNull ItemStack> gasInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getGasTank(), GasInventorySlot.getCapability(stack));
        Predicate<@NonNull ItemStack> infusionInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getInfusionTank(), InfusionInventorySlot.getCapability(stack));
        Predicate<@NonNull ItemStack> pigmentInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getPigmentTank(), PigmentInventorySlot.getCapability(stack));
        Predicate<@NonNull ItemStack> slurryInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getSlurryTank(), SlurryInventorySlot.getCapability(stack));
        return new MergedChemicalInventorySlot<>(chemicalTank, (stack, automationType) -> {
            if (automationType == AutomationType.MANUAL) {
                //Always allow the player to manually extract
                return true;
            }
            Current current = chemicalTank.getCurrent();
            if (current == Current.GAS) {
                return gasExtractPredicate.test(stack);
            } else if (current == Current.INFUSION) {
                return infusionExtractPredicate.test(stack);
            } else if (current == Current.PIGMENT) {
                return pigmentExtractPredicate.test(stack);
            } else if (current == Current.SLURRY) {
                return slurryExtractPredicate.test(stack);
            }//Else the tank is empty, check all our extraction predicates
            return gasExtractPredicate.test(stack) && infusionExtractPredicate.test(stack) && pigmentExtractPredicate.test(stack) && slurryExtractPredicate.test(stack);
        }, (stack, automationType) -> {
            Current current = chemicalTank.getCurrent();
            if (current == Current.GAS) {
                return gasInsertPredicate.test(stack);
            } else if (current == Current.INFUSION) {
                return infusionInsertPredicate.test(stack);
            } else if (current == Current.PIGMENT) {
                return pigmentInsertPredicate.test(stack);
            } else if (current == Current.SLURRY) {
                return slurryInsertPredicate.test(stack);
            }//Else the tank is empty, only allow it if one of the chemical insert predicates matches
            return gasInsertPredicate.test(stack) || infusionInsertPredicate.test(stack) || pigmentInsertPredicate.test(stack) || slurryInsertPredicate.test(stack);
        }, MergedChemicalInventorySlot::hasCapability, listener, x, y);
    }

    protected final MERGED mergedTank;

    protected MergedChemicalInventorySlot(MERGED mergedTank, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
        this.mergedTank = mergedTank;
    }

    /**
     * Drains tank into slot (tries all types)
     */
    public void drainChemicalTanks() {
        drainChemicalTank(CurrentType.GAS);
        drainChemicalTank(CurrentType.INFUSION);
        drainChemicalTank(CurrentType.PIGMENT);
        drainChemicalTank(CurrentType.SLURRY);
    }

    /**
     * Drains tank into slot
     */
    public void drainChemicalTank(CurrentType type) {
        if (type == CurrentType.GAS) {
            ChemicalInventorySlot.drainChemicalTank(this, mergedTank.getGasTank(), GasInventorySlot.getCapability(current));
        } else if (type == CurrentType.INFUSION) {
            ChemicalInventorySlot.drainChemicalTank(this, mergedTank.getInfusionTank(), InfusionInventorySlot.getCapability(current));
        } else if (type == CurrentType.PIGMENT) {
            ChemicalInventorySlot.drainChemicalTank(this, mergedTank.getPigmentTank(), PigmentInventorySlot.getCapability(current));
        } else if (type == CurrentType.SLURRY) {
            ChemicalInventorySlot.drainChemicalTank(this, mergedTank.getSlurryTank(), SlurryInventorySlot.getCapability(current));
        }
    }

    /**
     * Fills tank from slot (tries all types)
     */
    public void fillChemicalTanks() {
        fillChemicalTank(CurrentType.GAS);
        fillChemicalTank(CurrentType.INFUSION);
        fillChemicalTank(CurrentType.PIGMENT);
        fillChemicalTank(CurrentType.SLURRY);
    }

    /**
     * Fills tank from slot
     */
    public void fillChemicalTank(CurrentType type) {
        if (type == CurrentType.GAS) {
            ChemicalInventorySlot.fillChemicalTank(this, mergedTank.getGasTank(), GasInventorySlot.getCapability(current));
        } else if (type == CurrentType.INFUSION) {
            ChemicalInventorySlot.fillChemicalTank(this, mergedTank.getInfusionTank(), InfusionInventorySlot.getCapability(current));
        } else if (type == CurrentType.PIGMENT) {
            ChemicalInventorySlot.fillChemicalTank(this, mergedTank.getPigmentTank(), PigmentInventorySlot.getCapability(current));
        } else if (type == CurrentType.SLURRY) {
            ChemicalInventorySlot.fillChemicalTank(this, mergedTank.getSlurryTank(), SlurryInventorySlot.getCapability(current));
        }
    }
}
