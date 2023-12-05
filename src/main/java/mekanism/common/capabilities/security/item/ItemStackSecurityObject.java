package mekanism.common.capabilities.security.item;

import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for implementing security on items
 */
@NothingNullByDefault
public class ItemStackSecurityObject extends ItemStackOwnerObject implements ISecurityObject {

    public static void attachCapsToItem(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (stack, ctx) -> new ItemStackSecurityObject(stack), item);
        event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (stack, ctx) -> new ItemStackSecurityObject(stack), item);
    }

    private ItemStackSecurityObject(ItemStack stack) {
        super(stack);
    }

    @Override
    public SecurityMode getSecurityMode() {
        return stack.isEmpty() ? SecurityMode.PUBLIC : SecurityMode.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.SECURITY_MODE));
    }

    @Override
    public void setSecurityMode(SecurityMode mode) {
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
        // as that requires the player that can change the security mode to be holding the item, so they are the only one who
        // could have it open. When override settings change we properly recheck if players should be kicked out
    }
}