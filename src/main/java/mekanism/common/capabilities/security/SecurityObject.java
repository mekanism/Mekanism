package mekanism.common.capabilities.security;

import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class SecurityObject extends OwnerObject implements ISecurityObject {

    public SecurityObject(ItemAccess itemAccess) {
        super(itemAccess);
    }

    @Override
    public SecurityMode getSecurityMode() {
        return itemAccess.getResource().getOrDefault(MekanismDataComponents.SECURITY, SecurityMode.PUBLIC);
    }

    @Override
    public void setSecurityMode(SecurityMode mode, @Nullable TransactionContext transaction) {
        ItemResource resource = itemAccess.getResource();
        SecurityMode securityMode = resource.getOrDefault(MekanismDataComponents.SECURITY, SecurityMode.PUBLIC);
        if (securityMode != mode) {
            if (mode == SecurityMode.PUBLIC) {
                ItemAccessUtils.exchange(itemAccess, resource.without(MekanismDataComponents.SECURITY), transaction);
            } else {
                ItemAccessUtils.exchange(itemAccess, resource.with(MekanismDataComponents.SECURITY, mode), transaction);
            }
            //Note: For now we don't bother booting players out of item containers if the security mode on the item itself changed
            // as that requires the player that can change the security mode to be holding the item, so they are the only one who
            // could have it open. When override settings change we properly recheck if players should be kicked out
        }
    }
}