package mekanism.common.capabilities.security;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IOwnerObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class OwnerObject implements IOwnerObject {

    protected final ItemStack stack;

    public OwnerObject(ItemStack stack) {
        this.stack = stack;
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return stack.get(MekanismDataComponents.OWNER);
    }

    @Nullable
    @Override
    public String getOwnerName() {
        UUID owner = getOwnerUUID();
        if (owner != null) {
            //Do our best effort to figure out what the owner's name is, but it is possible we won't be able to calculate one
            return OwnerDisplay.getOwnerName(MekanismUtils.tryGetClientPlayer(), owner, null);
        }
        return null;
    }

    @Override
    public void setOwnerUUID(@Nullable UUID owner) {
        UUID ownerUUID = getOwnerUUID();
        if (!Objects.equals(ownerUUID, owner)) {
            if (ownerUUID != null) {
                //If the object happens to be a frequency aware object reset the frequency when the owner changes
                stack.remove(MekanismDataComponents.INVENTORY_FREQUENCY);
                stack.remove(MekanismDataComponents.TELEPORTER_FREQUENCY);
                stack.remove(MekanismDataComponents.QIO_FREQUENCY);
            }
            stack.set(MekanismDataComponents.OWNER, owner);
        }
    }
}