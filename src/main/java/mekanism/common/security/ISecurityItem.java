package mekanism.common.security;

import mekanism.common.security.ISecurityTile.SecurityMode;
import net.minecraft.item.ItemStack;

public interface ISecurityItem extends IOwnerItem {

    SecurityMode getSecurity(ItemStack stack);

    void setSecurity(ItemStack stack, SecurityMode mode);

    //TODO: Is this still needed/useful
    default boolean hasSecurity(ItemStack stack) {
        return true;
    }

    @Override
    default boolean hasOwner(ItemStack stack) {
        return hasSecurity(stack);
    }
}