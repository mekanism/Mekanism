package mekanism.common.network;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.network.PacketRemoveUpgrade.RemoveUpgradeMessage;
import mekanism.common.tile.TileEntityBasicBlock;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketRemoveUpgrade implements IMessageHandler<RemoveUpgradeMessage, IMessage>
{
	@Override
	public IMessage onMessage(RemoveUpgradeMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		TileEntity tileEntity = message.coord4D.getTileEntity(player.worldObj);
		
		if(tileEntity instanceof IUpgradeTile && tileEntity instanceof TileEntityBasicBlock)
		{
			IUpgradeTile upgradeTile = (IUpgradeTile)tileEntity;
			Upgrade upgrade = Upgrade.values()[message.upgradeType];

			if(upgradeTile.getComponent().getUpgrades(upgrade) > 0)
			{
				if(player.inventory.addItemStackToInventory(upgrade.getStack()))
				{
					upgradeTile.getComponent().removeUpgrade(upgrade);
				}
			}
		}
		
		return null;
	}
	
	public static class RemoveUpgradeMessage implements IMessage
	{
		public Coord4D coord4D;
	
		public int upgradeType;
		
		public RemoveUpgradeMessage() {}
	
		public RemoveUpgradeMessage(Coord4D coord, int type)
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
	
			dataStream.writeInt(upgradeType);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
			
			upgradeType = dataStream.readInt();
		}
	}
}
