package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.security.SecurityFrequency;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SecurityInventorySlot extends BasicInventorySlot {

    public static final Predicate<@NotNull ItemStack> VALIDATOR = stack -> IItemSecurityUtils.INSTANCE.ownerCapability(stack) != null;
    public static final Predicate<@NotNull ItemStack> LOCK_EXTRACT_PREDICATE = stack -> IItemSecurityUtils.INSTANCE.getOwnerUUID(stack) != null;
    public static final Predicate<@NotNull ItemStack> LOCK_INSERT_PREDICATE = stack -> IItemSecurityUtils.INSTANCE.getOwnerUUID(stack) == null;

    public static SecurityInventorySlot unlock(Supplier<UUID> ownerSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(ownerSupplier, "Owner supplier cannot be null");
        return new SecurityInventorySlot(LOCK_INSERT_PREDICATE, stack -> {
            UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
            return ownerUUID != null && ownerUUID.equals(ownerSupplier.get());
        }, listener, x, y);
    }

    public static SecurityInventorySlot lock(@Nullable IContentsListener listener, int x, int y) {
        return new SecurityInventorySlot(LOCK_EXTRACT_PREDICATE, LOCK_INSERT_PREDICATE, listener, x, y);
    }

    private SecurityInventorySlot(Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, VALIDATOR, listener, x, y);
    }

    public void unlock(@NotNull UUID ownerUUID) {
        if (!isEmpty()) {
            IOwnerObject ownerObject = IItemSecurityUtils.INSTANCE.ownerCapability(current);
            if (ownerObject != null) {
                UUID stackOwner = ownerObject.getOwnerUUID();
                if (ownerUUID.equals(stackOwner)) {
                    ownerObject.setOwnerUUID(null);
                    ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(current);
                    if (securityObject != null) {
                        securityObject.setSecurityMode(SecurityMode.PUBLIC);
                    }
                }
            }
        }
    }

    public void lock(UUID ownerUUID, SecurityFrequency frequency) {
        if (!isEmpty()) {
            IOwnerObject ownerObject = IItemSecurityUtils.INSTANCE.ownerCapability(current);
            if (ownerObject != null) {
                UUID stackOwner = ownerObject.getOwnerUUID();
                if (stackOwner == null) {
                    ownerObject.setOwnerUUID(stackOwner = ownerUUID);
                }
                if (stackOwner.equals(ownerUUID)) {
                    ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(current);
                    if (securityObject != null) {
                        securityObject.setSecurityMode(frequency.getSecurity());
                    }
                }
            }
        }
    }
}