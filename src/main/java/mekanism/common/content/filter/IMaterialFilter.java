package mekanism.common.content.filter;

import net.minecraft.item.ItemStack;

public interface IMaterialFilter extends IFilter {

    ItemStack getMaterialItem();

    void setMaterialItem(ItemStack stack);
}