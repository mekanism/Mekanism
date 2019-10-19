package mekanism.common.inventory.slot;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.security.IOwnerItem;
import net.minecraft.item.ItemStack;

//TODO: Switch IOwnerItem to being a capability
public class SecurityInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = stack -> stack.getItem() instanceof IOwnerItem;

    public static SecurityInventorySlot unlock(Supplier<UUID> ownerSupplier, IMekanismInventory inventory, int x, int y) {
        return new SecurityInventorySlot(stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) == null, stack -> {
            UUID ownerUUID = ((IOwnerItem) stack.getItem()).getOwnerUUID(stack);
            return ownerUUID != null && ownerUUID.equals(ownerSupplier.get());
        }, inventory, x, y);
    }

    public static SecurityInventorySlot lock(IMekanismInventory inventory, int x, int y) {
        return new SecurityInventorySlot(stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) != null,
              stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) == null, inventory, x, y);
    }

    private SecurityInventorySlot(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
    }
}