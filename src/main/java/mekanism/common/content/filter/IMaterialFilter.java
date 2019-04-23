package mekanism.common.content.filter;

import net.minecraft.item.ItemStack;

public interface IMaterialFilter {
    ItemStack getMaterialItem();

    void setMaterialItem(ItemStack stack);
}