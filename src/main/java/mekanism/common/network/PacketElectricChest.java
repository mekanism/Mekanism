package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IElectricChest;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.inventory.InventoryElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestMessage;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketElectricChest implements IMessageHandler<ElectricChestMessage, IMessage>
{
	@Override
	public IMessage onMessage(ElectricChestMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(message.packetType == ElectricChestPacketType.SERVER_OPEN)
		{
			try {
				if(message.isBlock)
				{
					TileEntityElectricChest tileEntity = (TileEntityElectricChest)message.coord4D.getTileEntity(player.worldObj);

					if(message.useEnergy)
					{
						tileEntity.setEnergy(tileEntity.getEnergy() - 100);
					}

					MekanismUtils.openElectricChestGui((EntityPlayerMP)player, tileEntity, null, true);
				}
				else {
					ItemStack stack = player.getCurrentEquippedItem();

					if(stack != null && stack.getItem() instanceof IElectricChest && BlockStateMachine.MachineType.get(stack) == BlockStateMachine.MachineType.ELECTRIC_CHEST)
					{
						if(message.useEnergy)
						{
							((IEnergizedItem)stack.getItem()).setEnergy(stack, ((IEnergizedItem)stack.getItem()).getEnergy(stack) - 100);
						}

						InventoryElectricChest inventory = new InventoryElectricChest(player);
						MekanismUtils.openElectricChestGui((EntityPlayerMP)player, null, inventory, false);
					}
				}
			} catch(Exception e) {
				Mekanism.logger.error("Error while handling electric chest open packet.");
				e.printStackTrace();
			}
		}
		else if(message.packetType == ElectricChestPacketType.CLIENT_OPEN)
		{
			try {
				Mekanism.proxy.openElectricChest(player, message.guiType, message.windowId, message.isBlock, message.coord4D != null ? message.coord4D : BlockPos.ORIGIN);
			} catch(Exception e) {
				Mekanism.logger.error("Error while handling electric chest open packet.");
				e.printStackTrace();
			}
		}
		else if(message.packetType == ElectricChestPacketType.PASSWORD)
		{
			try {
				if(message.isBlock)
				{
					TileEntityElectricChest tileEntity = (TileEntityElectricChest)message.coord4D.getTileEntity(player.worldObj);
					tileEntity.password = message.password;
					tileEntity.authenticated = true;
				}
				else {
					ItemStack stack = player.getCurrentEquippedItem();

					if(stack != null && stack.getItem() instanceof IElectricChest && BlockStateMachine.MachineType.get(stack) == BlockStateMachine.MachineType.ELECTRIC_CHEST)
					{
						((IElectricChest)stack.getItem()).setPassword(stack, message.password);
						((IElectricChest)stack.getItem()).setAuthenticated(stack, true);
					}
				}
			} catch(Exception e) {
				Mekanism.logger.error("Error while handling electric chest password packet.");
				e.printStackTrace();
			}
		}
		else if(message.packetType == ElectricChestPacketType.LOCK)
		{
			try {
				if(message.isBlock)
				{
					TileEntityElectricChest tileEntity = (TileEntityElectricChest)message.coord4D.getTileEntity(player.worldObj);
					tileEntity.locked = message.locked;
					player.worldObj.notifyNeighborsOfStateChange(message.coord4D, tileEntity.getBlockType());
				}
				else {
					ItemStack stack = player.getCurrentEquippedItem();

					if(stack != null && stack.getItem() instanceof IElectricChest && BlockStateMachine.MachineType.get(stack) == BlockStateMachine.MachineType.ELECTRIC_CHEST)
					{
						((IElectricChest)stack.getItem()).setLocked(stack, message.locked);
					}
				}
			} catch(Exception e) {
				Mekanism.logger.error("Error while handling electric chest password packet.");
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static class ElectricChestMessage implements IMessage
	{
		public ElectricChestPacketType packetType;
	
		public boolean isBlock;
	
		public boolean locked;
	
		public String password;
	
		public int guiType;
		public int windowId;
	
		public boolean useEnergy;
	
		public Coord4D coord4D;
		
		public ElectricChestMessage() {}
	
		//This is a really messy implementation...
		public ElectricChestMessage(ElectricChestPacketType type, boolean b1, boolean b2, int i1, int i2, String s1, Coord4D c1)
		{
			packetType = type;
	
			switch(packetType)
			{
				case LOCK:
					locked = b1;
					isBlock = b2;
	
					if(isBlock)
					{
						coord4D = c1;
					}
	
					break;
				case PASSWORD:
					password = s1;
					isBlock = b1;
	
					if(isBlock)
					{
						coord4D = c1;
					}
	
					break;
				case CLIENT_OPEN:
					guiType = i1;
					windowId = i2;
					isBlock = b1;
	
					if(isBlock)
					{
						coord4D = c1;
					}
	
					break;
				case SERVER_OPEN:
					useEnergy = b1;
					isBlock = b2;
	
					if(isBlock)
					{
						coord4D = c1;
					}
	
					break;
			}
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(packetType.ordinal());
	
			switch(packetType)
			{
				case LOCK:
					dataStream.writeBoolean(locked);
					dataStream.writeBoolean(isBlock);
	
					if(isBlock)
					{
						coord4D.write(dataStream);
					}
	
					break;
				case PASSWORD:
					PacketHandler.writeString(dataStream, password);
					dataStream.writeBoolean(isBlock);
	
					if(isBlock)
					{
						coord4D.write(dataStream);
					}
	
					break;
				case CLIENT_OPEN:
					dataStream.writeInt(guiType);
					dataStream.writeInt(windowId);
					dataStream.writeBoolean(isBlock);
	
					if(isBlock)
					{
						coord4D.write(dataStream);
					}
	
					break;
				case SERVER_OPEN:
					dataStream.writeBoolean(useEnergy);
					dataStream.writeBoolean(isBlock);
	
					if(isBlock)
					{
						coord4D.write(dataStream);
					}
	
					break;
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = ElectricChestPacketType.values()[dataStream.readInt()];
	
			if(packetType == ElectricChestPacketType.SERVER_OPEN)
			{
				useEnergy = dataStream.readBoolean();
				isBlock = dataStream.readBoolean();

				if(isBlock)
				{
					coord4D = Coord4D.read(dataStream);
				}
			}
			else if(packetType == ElectricChestPacketType.CLIENT_OPEN)
			{
				guiType = dataStream.readInt();
				windowId = dataStream.readInt();
				isBlock = dataStream.readBoolean();

				if(isBlock)
				{
					coord4D = Coord4D.read(dataStream);
				}
			}
			else if(packetType == ElectricChestPacketType.PASSWORD)
			{
				password = PacketHandler.readString(dataStream);
				isBlock = dataStream.readBoolean();

				if(isBlock)
				{
					coord4D = Coord4D.read(dataStream);
				}
			}
			else if(packetType == ElectricChestPacketType.LOCK)
			{
				locked = dataStream.readBoolean();
				isBlock = dataStream.readBoolean();

				if(isBlock)
				{
					coord4D = Coord4D.read(dataStream);
				}
			}
		}
	}
	
	public static enum ElectricChestPacketType
	{
		LOCK,
		PASSWORD,
		CLIENT_OPEN,
		SERVER_OPEN
	}
}
