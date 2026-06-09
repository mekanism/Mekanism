package mekanism.common.capabilities.security;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.security.IOwnerObject;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class OwnerObject implements IOwnerObject {

    protected final ItemAccess itemAccess;

    public OwnerObject(ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return itemAccess.getResource().get(MekanismDataComponents.OWNER);
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
    public void setOwnerUUID(@Nullable UUID owner, @Nullable TransactionContext transaction) {
        ItemResource resource = itemAccess.getResource();
        UUID ownerUUID = resource.get(MekanismDataComponents.OWNER);
        if (!Objects.equals(ownerUUID, owner)) {
            if (ownerUUID != null) {
                //If the object happens to be a frequency aware object reset the frequency when the owner changes
                resource = resource.without(MekanismDataComponents.INVENTORY_FREQUENCY)
                      .without(MekanismDataComponents.TELEPORTER_FREQUENCY)
                      .without(MekanismDataComponents.QIO_FREQUENCY);
            }
            ItemAccessUtils.exchange(itemAccess, resource.with(MekanismDataComponents.OWNER, owner), transaction);
        }
    }
}