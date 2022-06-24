package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.security.SecurityFrequency;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SecurityInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NotNull ItemStack> validator = stack -> stack.getCapability(Capabilities.OWNER_OBJECT).isPresent();

    public static SecurityInventorySlot unlock(Supplier<UUID> ownerSupplier, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(ownerSupplier, "Owner supplier cannot be null");
        return new SecurityInventorySlot(stack -> MekanismAPI.getSecurityUtils().getOwnerUUID(stack) == null, stack -> {
            UUID ownerUUID = MekanismAPI.getSecurityUtils().getOwnerUUID(stack);
            return ownerUUID != null && ownerUUID.equals(ownerSupplier.get());
        }, listener, x, y);
    }

    public static SecurityInventorySlot lock(@Nullable IContentsListener listener, int x, int y) {
        Predicate<@NotNull ItemStack> insertPredicate = stack -> MekanismAPI.getSecurityUtils().getOwnerUUID(stack) == null;
        return new SecurityInventorySlot(insertPredicate.negate(), insertPredicate, listener, x, y);
    }

    private SecurityInventorySlot(Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
    }

    public void unlock(UUID ownerUUID) {
        if (!isEmpty()) {
            current.getCapability(Capabilities.OWNER_OBJECT).ifPresent(ownerObject -> {
                UUID stackOwner = ownerObject.getOwnerUUID();
                if (stackOwner != null && stackOwner.equals(ownerUUID)) {
                    ownerObject.setOwnerUUID(null);
                    current.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(securityObject -> securityObject.setSecurityMode(SecurityMode.PUBLIC));
                }
            });
        }
    }

    public void lock(UUID ownerUUID, SecurityFrequency frequency) {
        if (!isEmpty()) {
            current.getCapability(Capabilities.OWNER_OBJECT).ifPresent(ownerObject -> {
                UUID stackOwner = ownerObject.getOwnerUUID();
                if (stackOwner == null) {
                    ownerObject.setOwnerUUID(stackOwner = ownerUUID);
                }
                if (stackOwner.equals(ownerUUID)) {
                    current.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(securityObject -> securityObject.setSecurityMode(frequency.getSecurityMode()));
                }
            });
        }
    }
}