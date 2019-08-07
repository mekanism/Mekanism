package mekanism.common.base;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public interface IItemNetwork {

    void handlePacketData(ItemStack stack, PacketBuffer dataStream);
}