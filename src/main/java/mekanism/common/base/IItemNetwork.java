package mekanism.common.base;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.IWorld;

public interface IItemNetwork {

    //TODO: Make this take a param for world
    void handlePacketData(IWorld world, ItemStack stack, PacketBuffer dataStream);
}