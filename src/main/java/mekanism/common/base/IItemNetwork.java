package mekanism.common.base;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

public interface IItemNetwork 
{
	public void handlePacketData(ItemStack stack, ByteBuf dataStream) throws Exception;
}
