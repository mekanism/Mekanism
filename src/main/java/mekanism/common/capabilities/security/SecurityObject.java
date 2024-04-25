package mekanism.common.capabilities.security;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class SecurityObject extends OwnerObject implements ISecurityObject {

    public SecurityObject(ItemStack stack) {
        super(stack);
    }

    @Override
    public SecurityMode getSecurityMode() {
        return stack.getOrDefault(MekanismDataComponents.SECURITY, SecurityMode.PUBLIC);
    }

    @Override
    public void setSecurityMode(SecurityMode mode) {
        SecurityMode securityMode = getSecurityMode();
        if (securityMode != mode) {
            if (mode == SecurityMode.PUBLIC) {
                stack.remove(MekanismDataComponents.SECURITY);
            } else {
                stack.set(MekanismDataComponents.SECURITY, mode);
            }
            onSecurityChanged(securityMode, mode);
        }
    }

    @Override
    public void onSecurityChanged(@NotNull SecurityMode old, @NotNull SecurityMode mode) {
        //Note: For now we don't bother booting players out of item containers if the security mode on the item itself changed
        // as that requires the player that can change the security mode to be holding the item, so they are the only one who
        // could have it open. When override settings change we properly recheck if players should be kicked out
    }
}