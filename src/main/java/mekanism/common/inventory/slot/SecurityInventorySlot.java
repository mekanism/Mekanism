package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SecurityInventorySlot extends BasicInventorySlot {

    public static final Predicate<ItemResource> VALIDATOR = itemType -> IItemSecurityUtils.INSTANCE.ownerCapability(ItemAccessUtils.queryOnlyAccess(itemType)) != null;
    public static final BiPredicate<ItemResource, AutomationType> LOCK_EXTRACT_PREDICATE = (itemType, automationType) ->
          !automationType.isExternal() || IItemSecurityUtils.INSTANCE.getOwnerUUID(ItemAccessUtils.queryOnlyAccess(itemType)) != null;
    public static final BiPredicate<ItemResource, AutomationType> LOCK_INSERT_PREDICATE = (itemType, automationType) ->
          //Allow inserting internally even if it doesn't match, so that we can replace the item via the item access
          automationType.isInternal() || IItemSecurityUtils.INSTANCE.getOwnerUUID(ItemAccessUtils.queryOnlyAccess(itemType)) == null;
    public static final BiPredicate<ItemResource, AutomationType> UNLOCK_EXTRACT_PREDICATE = (itemType, automationType) ->
          !automationType.isExternal() || IItemSecurityUtils.INSTANCE.getOwnerUUID(ItemAccessUtils.queryOnlyAccess(itemType)) == null;

    public static SecurityInventorySlot unlock(Supplier<UUID> ownerSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(ownerSupplier, "Owner supplier cannot be null");
        return new SecurityInventorySlot(UNLOCK_EXTRACT_PREDICATE, (itemType, automationType) -> canInsertUnlock(itemType, automationType, ownerSupplier),
              listener, x, y);
    }

    public static boolean canInsertUnlock(ItemResource itemType, AutomationType automationType, Supplier<UUID> ownerSupplier) {
        if (automationType.isInternal()) {
            //Allow inserting internally even if it doesn't match, so that we can replace the item via the item access
            return true;
        }
        UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(ItemAccessUtils.queryOnlyAccess(itemType));
        return ownerUUID != null && ownerUUID.equals(ownerSupplier.get());
    }

    public static SecurityInventorySlot lock(@Nullable IContentsListener listener, int x, int y) {
        return new SecurityInventorySlot(LOCK_EXTRACT_PREDICATE, LOCK_INSERT_PREDICATE, listener, x, y);
    }

    private SecurityInventorySlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, VALIDATOR, listener, x, y);
    }

    public void unlock(UUID ownerUUID, TransactionContext transaction) {
        if (!isEmpty()) {
            ItemAccess itemAccess = asItemAccess();
            IOwnerObject ownerObject = IItemSecurityUtils.INSTANCE.ownerCapability(itemAccess);
            if (ownerObject != null) {
                UUID stackOwner = ownerObject.getOwnerUUID();
                if (ownerUUID.equals(stackOwner)) {
                    ownerObject.setOwnerUUID(null, transaction);
                    ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(itemAccess);
                    if (securityObject != null) {
                        securityObject.setSecurityMode(SecurityMode.PUBLIC, transaction);
                    }
                }
            }
        }
    }

    public void lock(UUID ownerUUID, SecurityFrequency frequency, TransactionContext transaction) {
        if (!isEmpty()) {
            ItemAccess itemAccess = asItemAccess();
            IOwnerObject ownerObject = IItemSecurityUtils.INSTANCE.ownerCapability(itemAccess);
            if (ownerObject != null) {
                UUID stackOwner = ownerObject.getOwnerUUID();
                if (stackOwner == null) {
                    stackOwner = ownerUUID;
                    ownerObject.setOwnerUUID(stackOwner, transaction);
                }
                if (stackOwner.equals(ownerUUID)) {
                    ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(itemAccess);
                    if (securityObject != null) {
                        securityObject.setSecurityMode(frequency.getSecurity(), transaction);
                    }
                }
            }
        }
    }
}