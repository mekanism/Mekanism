package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.infuse.InfusionTank;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfusionInventorySlot extends BasicInventorySlot {

    //TODO: Rewrite this some once we make infusion tanks work as items
    public static InfusionInventorySlot input(InfusionTank infusionTank, Predicate<InfuseType> isValidInfusion, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(isValidInfusion, "Infusion validity check cannot be null");
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
        }, inventory, x, y);
    }

    //TODO: Replace InfusionTank with an IInfusionHandler??
    private final InfusionTank infusionTank;

    private InfusionInventorySlot(InfusionTank infusionTank, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        this.infusionTank = infusionTank;
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.EXTRA;
    }

    //TODO: Make it so that the infusion tank fills
}