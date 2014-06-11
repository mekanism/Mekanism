package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.IUpgradeManagement;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketRemoveUpgrade.RemoveUpgradeMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketRemoveUpgrade implements IMessageHandler<RemoveUpgradeMessage, IMessage>
{
	@Override
	public IMessage onMessage(RemoveUpgradeMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		TileEntity tileEntity = message.coord4D.getTileEntity(player.worldObj);
		
		if(tileEntity instanceof IUpgradeManagement && tileEntity instanceof TileEntityBasicBlock)
		{
			IUpgradeManagement upgradeTile = (IUpgradeManagement)tileEntity;

			if(message.upgradeType == 0)
			{
				if(upgradeTile.getSpeedMultiplier() > 0)
				{
					if(player.inventory.addItemStackToInventory(new ItemStack(Mekanism.SpeedUpgrade)))
					{
						upgradeTile.setSpeedMultiplier(upgradeTile.getSpeedMultiplier()-1);
					}
				}
			}
			else if(message.upgradeType == 1)
			{
				if(upgradeTile.getEnergyMultiplier() > 0)
				{
					if(player.inventory.addItemStackToInventory(new ItemStack(Mekanism.EnergyUpgrade)))
					{
						upgradeTile.setEnergyMultiplier(upgradeTile.getEnergyMultiplier()-1);
					}
				}
			}
		}
		
		return null;
	}
	
	public static class RemoveUpgradeMessage implements IMessage
	{
		public Coord4D coord4D;
	
		public byte upgradeType;
		
		public RemoveUpgradeMessage() {}
	
		public RemoveUpgradeMessage(Coord4D coord, byte type)
		{
			coord4D = coord;
			upgradeType = type;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(coord4D.xCoord);
			dataStream.writeInt(coord4D.yCoord);
			dataStream.writeInt(coord4D.zCoord);
			dataStream.writeInt(coord4D.dimensionId);
	
			dataStream.writeByte(upgradeType);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
			
			upgradeType = dataStream.readByte();
		}
	}
}
