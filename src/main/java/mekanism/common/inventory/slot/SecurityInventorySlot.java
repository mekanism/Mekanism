package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.security.IOwnerItem;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.security.SecurityFrequency;
import net.minecraft.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SecurityInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = stack -> stack.getItem() instanceof IOwnerItem;

    public static SecurityInventorySlot unlock(Supplier<UUID> ownerSupplier, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(ownerSupplier, "Owner supplier cannot be null");
        return new SecurityInventorySlot(stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) == null, stack -> {
            UUID ownerUUID = ((IOwnerItem) stack.getItem()).getOwnerUUID(stack);
            return ownerUUID != null && ownerUUID.equals(ownerSupplier.get());
        }, inventory, x, y);
    }

    public static SecurityInventorySlot lock(@Nullable IMekanismInventory inventory, int x, int y) {
        return new SecurityInventorySlot(stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) != null,
              stack -> ((IOwnerItem) stack.getItem()).getOwnerUUID(stack) == null, inventory, x, y);
    }

    private SecurityInventorySlot(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
    }

    public void unlock(UUID ownerUUID) {
        if (!isEmpty() && current.getItem() instanceof IOwnerItem) {
            IOwnerItem item = (IOwnerItem) current.getItem();
            UUID stackOwner = item.getOwnerUUID(current);
            if (stackOwner != null && stackOwner.equals(ownerUUID)) {
                item.setOwnerUUID(current, null);
                if (item instanceof ISecurityItem) {
                    ((ISecurityItem) item).setSecurity(current, SecurityMode.PUBLIC);
                }
            }
        }
    }

    public void lock(UUID ownerUUID, SecurityFrequency frequency) {
        if (!isEmpty() && current.getItem() instanceof IOwnerItem) {
            IOwnerItem item = (IOwnerItem) current.getItem();
            UUID stackOwner = item.getOwnerUUID(current);
            if (stackOwner == null) {
                item.setOwnerUUID(current, stackOwner = ownerUUID);
            }
            if (stackOwner.equals(ownerUUID)) {
                if (item instanceof ISecurityItem) {
                    ((ISecurityItem) item).setSecurity(current, frequency.securityMode);
                }
            }
        }
    }
}