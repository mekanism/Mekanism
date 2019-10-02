package mekanism.common.security;

import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;

public interface ISecurityItem extends IOwnerItem {

    default SecurityMode getSecurity(@Nonnull ItemStack stack) {
        if (!MekanismConfig.general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }
        return EnumUtils.SECURITY_MODES[ItemDataUtils.getInt(stack, "security")];
    }

    default void setSecurity(@Nonnull ItemStack stack, SecurityMode mode) {
        ItemDataUtils.setInt(stack, "security", mode.ordinal());
    }
}