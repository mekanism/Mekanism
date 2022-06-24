package mekanism.common.inventory.slot.chemical;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MergedChemicalInventorySlot<MERGED extends MergedChemicalTank> extends BasicInventorySlot {

    private static boolean hasCapability(@NotNull ItemStack stack) {
        return stack.getCapability(Capabilities.GAS_HANDLER).isPresent() || stack.getCapability(Capabilities.INFUSION_HANDLER).isPresent() ||
               stack.getCapability(Capabilities.PIGMENT_HANDLER).isPresent() || stack.getCapability(Capabilities.SLURRY_HANDLER).isPresent();
    }

    public static MergedChemicalInventorySlot<MergedChemicalTank> drain(MergedChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Merged chemical tank cannot be null");
        Predicate<@NotNull ItemStack> gasInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getGasTank(), GasInventorySlot::getCapability);
        Predicate<@NotNull ItemStack> infusionInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getInfusionTank(), InfusionInventorySlot::getCapability);
        Predicate<@NotNull ItemStack> pigmentInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getPigmentTank(), PigmentInventorySlot::getCapability);
        Predicate<@NotNull ItemStack> slurryInsertPredicate = ChemicalInventorySlot.getDrainInsertPredicate(chemicalTank.getSlurryTank(), SlurryInventorySlot::getCapability);
        BiPredicate<@NotNull ItemStack, @NotNull AutomationType> insertPredicate = (stack, automationType) -> switch (chemicalTank.getCurrent()) {
            case GAS -> gasInsertPredicate.test(stack);
            case INFUSION -> infusionInsertPredicate.test(stack);
            case PIGMENT -> pigmentInsertPredicate.test(stack);
            case SLURRY -> slurryInsertPredicate.test(stack);
            //Tank is empty, check if any insert predicate is valid
            case EMPTY -> gasInsertPredicate.test(stack) || infusionInsertPredicate.test(stack) || pigmentInsertPredicate.test(stack) ||
                          slurryInsertPredicate.test(stack);
        };
        //Extract predicate, always allow the player to manually extract or if the insert predicate no longer matches allow for it to be extracted
        return new MergedChemicalInventorySlot<>(chemicalTank, (stack, automationType) -> automationType == AutomationType.MANUAL || !insertPredicate.test(stack, automationType),
              insertPredicate, MergedChemicalInventorySlot::hasCapability, listener, x, y);
    }

    public static MergedChemicalInventorySlot<MergedChemicalTank> fill(MergedChemicalTank chemicalTank, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(chemicalTank, "Merged chemical tank cannot be null");
        Predicate<@NotNull ItemStack> gasExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getGasTank(), GasInventorySlot::getCapability);
        Predicate<@NotNull ItemStack> infusionExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getInfusionTank(), InfusionInventorySlot::getCapability);
        Predicate<@NotNull ItemStack> pigmentExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getPigmentTank(), PigmentInventorySlot::getCapability);
        Predicate<@NotNull ItemStack> slurryExtractPredicate = ChemicalInventorySlot.getFillExtractPredicate(chemicalTank.getSlurryTank(), SlurryInventorySlot::getCapability);
        Predicate<@NotNull ItemStack> gasInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getGasTank(), GasInventorySlot.getCapability(stack));
        Predicate<@NotNull ItemStack> infusionInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getInfusionTank(), InfusionInventorySlot.getCapability(stack));
        Predicate<@NotNull ItemStack> pigmentInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getPigmentTank(), PigmentInventorySlot.getCapability(stack));
        Predicate<@NotNull ItemStack> slurryInsertPredicate = stack -> ChemicalInventorySlot.fillInsertCheck(chemicalTank.getSlurryTank(), SlurryInventorySlot.getCapability(stack));
        return new MergedChemicalInventorySlot<>(chemicalTank, (stack, automationType) -> {
            if (automationType == AutomationType.MANUAL) {
                //Always allow the player to manually extract
                return true;
            }
            return switch (chemicalTank.getCurrent()) {
                case GAS -> gasExtractPredicate.test(stack);
                case INFUSION -> infusionExtractPredicate.test(stack);
                case PIGMENT -> pigmentExtractPredicate.test(stack);
                case SLURRY -> slurryExtractPredicate.test(stack);
                //Tank is empty, check all our extraction predicates
                case EMPTY -> gasExtractPredicate.test(stack) && infusionExtractPredicate.test(stack) && pigmentExtractPredicate.test(stack) &&
                              slurryExtractPredicate.test(stack);
            };
        }, (stack, automationType) -> switch (chemicalTank.getCurrent()) {
            case GAS -> gasInsertPredicate.test(stack);
            case INFUSION -> infusionInsertPredicate.test(stack);
            case PIGMENT -> pigmentInsertPredicate.test(stack);
            case SLURRY -> slurryInsertPredicate.test(stack);
            //Tank is empty, only allow it if one of the chemical insert predicates matches
            case EMPTY -> gasInsertPredicate.test(stack) || infusionInsertPredicate.test(stack) || pigmentInsertPredicate.test(stack) ||
                          slurryInsertPredicate.test(stack);
        }, MergedChemicalInventorySlot::hasCapability, listener, x, y);
    }

    protected final MERGED mergedTank;

    protected MergedChemicalInventorySlot(MERGED mergedTank, BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert, Predicate<@NotNull ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
        setSlotType(ContainerSlotType.EXTRA);
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
