package mekanism.common.lib.security;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.security.ISecurityTile.SecurityMode;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;

public interface ISecurityItem extends IOwnerItem {

    default SecurityMode getSecurity(@Nonnull ItemStack stack) {
        if (!MekanismConfig.general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }
        return SecurityMode.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.SECURITY_MODE));
    }

    default void setSecurity(@Nonnull ItemStack stack, SecurityMode mode) {
        ItemDataUtils.setInt(stack, NBTConstants.SECURITY_MODE, mode.ordinal());
    }
}