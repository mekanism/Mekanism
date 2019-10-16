package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.infuse.InfusionTank;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.item.ItemStack;

public class InfusionInventorySlot extends BasicInventorySlot {

    //TODO: Rewrite this some once we make infusion tanks work as items
    public static InfusionInventorySlot input(InfusionTank infusionTank, Predicate<InfuseType> isValidInfusion, int x, int y) {
        return new InfusionInventorySlot(infusionTank, stack -> {
            InfusionStack infusionStack = InfuseRegistry.getObject(stack);
            //Allow extraction IFF after a reload an item no longer has an infusion type
            return infusionStack.isEmpty() || !isValidInfusion.test(infusionStack.getType());
        }, stack -> {
            InfusionStack infusionStack = InfuseRegistry.getObject(stack);
            return !infusionStack.isEmpty() && (infusionTank.isEmpty() || infusionStack.isTypeEqual(infusionTank.getType()));
        }, stack -> {
            InfusionStack infusionStack = InfuseRegistry.getObject(stack);
            return !infusionStack.isEmpty() && isValidInfusion.test(infusionStack.getType());
        }, x, y);
    }

    //TODO: Replace InfusionTank with an IInfusionHandler??
    private final InfusionTank infusionTank;

    private InfusionInventorySlot(InfusionTank infusionTank, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          @Nonnull Predicate<@NonNull ItemStack> validator, int x, int y) {
        super(canExtract, canInsert, validator, x, y);
        this.infusionTank = infusionTank;
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.EXTRA;
    }

    //TODO: Make it so that the infusion tank fills
}