package mekanism.common.capabilities.security.item;

import java.util.function.Consumer;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for implementing security on items
 */
@NothingNullByDefault
public class ItemStackSecurityObject extends ItemStackOwnerObject implements ISecurityObject {

    @Override
    public SecurityMode getSecurityMode() {
        ItemStack stack = getStack();
        return stack.isEmpty() ? SecurityMode.PUBLIC : SecurityMode.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.SECURITY_MODE));
    }

    @Override
    public void setSecurityMode(SecurityMode mode) {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            SecurityMode current = getSecurityMode();
            if (current != mode) {
                ItemDataUtils.setInt(stack, NBTConstants.SECURITY_MODE, mode.ordinal());
                onSecurityChanged(current, mode);
            }
        }
    }

    @Override
    public void onSecurityChanged(@NotNull SecurityMode old, @NotNull SecurityMode mode) {
        //Note: For now we don't bother booting players out of item containers if the security mode on the item itself changed
        // as that requires the player that can change the security mode to be holding the item so they are the only one who
        // could have it open. When override settings change we properly recheck if players should be kicked out
    }

    @Override
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        //Note: We intentionally don't call super and instead handle resolving owner capabilities via a combined capability resolver
        // that handles owner and security items
        consumer.accept(BasicCapabilityResolver.security(this));
    }
}