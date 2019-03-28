package mekanism.common.security;

import mekanism.common.security.ISecurityTile.SecurityMode;
import net.minecraft.item.ItemStack;

public interface ISecurityItem extends IOwnerItem {

    SecurityMode getSecurity(ItemStack stack);

    void setSecurity(ItemStack stack, SecurityMode mode);

    boolean hasSecurity(ItemStack stack);
}
