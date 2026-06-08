package mekanism.common.item.interfaces;

import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.transfer.item.ItemResource;

public interface IGuiItem {

    ContainerTypeRegistryObject<?> getContainerType();

    default void encodeContainerData(RegistryFriendlyByteBuf buf, ItemResource itemType) {
    }
}
