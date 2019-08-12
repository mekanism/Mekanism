package mekanism.common.security;

import javax.annotation.Nonnull;
import mekanism.common.config_old.MekanismConfigOld;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;

public interface ISecurityItem extends IOwnerItem {

    default SecurityMode getSecurity(@Nonnull ItemStack stack) {
        if (!MekanismConfigOld.current().general.allowProtection.get()) {
            return SecurityMode.PUBLIC;
        }
        return SecurityMode.values()[ItemDataUtils.getInt(stack, "security")];
    }

    default void setSecurity(@Nonnull ItemStack stack, SecurityMode mode) {
        ItemDataUtils.setInt(stack, "security", mode.ordinal());
    }
}