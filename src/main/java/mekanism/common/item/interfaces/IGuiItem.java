package mekanism.common.item.interfaces;

import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface IGuiItem {

    ContainerTypeRegistryObject<?> getContainerType();

    default void encodeContainerData(RegistryFriendlyByteBuf buf, ItemStack stack) {
    }
}
