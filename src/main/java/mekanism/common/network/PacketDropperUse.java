package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITankManager.DropperHandler;
import mekanism.common.network.PacketDropperUse.DropperUseMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDropperUse implements IMessageHandler<DropperUseMessage, IMessage>
{
	@Override
	public IMessage onMessage(DropperUseMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		PacketHandler.handlePacket(new Runnable() {
			@Override
			public void run()
			{
				TileEntity tileEntity = message.coord4D.getTileEntity(player.worldObj);
				
				if(tileEntity instanceof ITankManager)
				{
					try {
						Object tank = ((ITankManager)tileEntity).getTanks()[message.tankId];
						
						if(tank != null)
						{
							DropperHandler.useDropper(player, tank, message.mouseButton);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}, player);
		
		return null;
	}
	
	public static class DropperUseMessage implements IMessage
	{
		public Coord4D coord4D;
		
		public int mouseButton;
		public int tankId;
		
		public DropperUseMessage() {}
	
		public DropperUseMessage(Coord4D coord, int button, int id)
		{
			coord4D = coord;
			mouseButton = button;
			tankId = id;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			coord4D.write(dataStream);
			
			dataStream.writeInt(mouseButton);
			dataStream.writeInt(tankId);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = Coord4D.read(dataStream);
			
			mouseButton = dataStream.readInt();
			tankId = dataStream.readInt();
		}
	}
}
