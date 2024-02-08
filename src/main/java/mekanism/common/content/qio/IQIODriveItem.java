package mekanism.common.content.qio;

import net.minecraft.world.item.ItemStack;

public interface IQIODriveItem {

    long getCountCapacity(ItemStack stack);

    int getTypeCapacity(ItemStack stack);
}
