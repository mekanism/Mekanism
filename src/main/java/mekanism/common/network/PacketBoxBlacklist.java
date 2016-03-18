package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.MekanismAPI;
import mekanism.api.util.BlockInfo;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketBoxBlacklist.BoxBlacklistMessage;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketBoxBlacklist implements IMessageHandler<BoxBlacklistMessage, IMessage>
{
	@Override
	public IMessage onMessage(BoxBlacklistMessage message, MessageContext context) 
	{
		return null;
	}
	   
	public static class BoxBlacklistMessage implements IMessage
	{
		public BoxBlacklistMessage() {}
		
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(MekanismAPI.getBoxIgnore().size());
	
			for(BlockInfo info : MekanismAPI.getBoxIgnore())
			{
				dataStream.writeInt(Block.getIdFromBlock(info.block));
				dataStream.writeInt(info.meta);
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			MekanismAPI.getBoxIgnore().clear();
	
			int amount = dataStream.readInt();
	
			for(int i = 0; i < amount; i++)
			{
				MekanismAPI.addBoxBlacklist(Block.getBlockById(dataStream.readInt()), dataStream.readInt());
			}
	
			Mekanism.logger.info("Received Cardboard Box blacklist entries from server (" + amount + " total)");
		}
	}
}
