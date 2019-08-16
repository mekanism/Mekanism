package mekanism.common.block.interfaces;

import net.minecraft.inventory.container.INamedContainerProvider;

public interface IHasGui {

    int getGuiID();

    INamedContainerProvider getProvider();
}