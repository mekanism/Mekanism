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
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SecurityInventorySlot extends BasicInventorySlot {

    //TODO - 26.1: Re-evaluate these and how we just make a stack out of the item type
    public static final Predicate<ItemResource> VALIDATOR = itemType -> IItemSecurityUtils.INSTANCE.ownerCapability(itemType.toStack()) != null;
    public static final Predicate<ItemResource> LOCK_EXTRACT_PREDICATE = itemType -> IItemSecurityUtils.INSTANCE.getOwnerUUID(itemType.toStack()) != null;
    public static final Predicate<ItemResource> LOCK_INSERT_PREDICATE = itemType -> IItemSecurityUtils.INSTANCE.getOwnerUUID(itemType.toStack()) == null;

    public static SecurityInventorySlot unlock(Supplier<UUID> ownerSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(ownerSupplier, "Owner supplier cannot be null");
        return new SecurityInventorySlot(LOCK_INSERT_PREDICATE, itemType -> {
            //TODO - 26.1: Re-evaluate how we just make a stack out of the item type
            UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(itemType.toStack());
            return ownerUUID != null && ownerUUID.equals(ownerSupplier.get());
        }, listener, x, y);
    }

    public static SecurityInventorySlot lock(@Nullable IContentsListener listener, int x, int y) {
        return new SecurityInventorySlot(LOCK_EXTRACT_PREDICATE, LOCK_INSERT_PREDICATE, listener, x, y);
    }

    private SecurityInventorySlot(Predicate<ItemResource> canExtract, Predicate<ItemResource> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, VALIDATOR, listener, x, y);
    }

    public void unlock(@NotNull UUID ownerUUID) {
        if (!isEmpty()) {
            //TODO - 26.1: Figure out and move item security utils to item access?
            ItemStack current = resource().toStack(amountAsInt());
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
            //TODO - 26.1: Figure out and move item security utils to item access?
            ItemStack current = resource().toStack(amountAsInt());
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