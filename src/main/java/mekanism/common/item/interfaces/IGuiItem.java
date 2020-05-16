package mekanism.common.item.interfaces;

import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface IGuiItem {

    INamedContainerProvider getContainerProvider(ItemStack stack, Hand hand);
}
