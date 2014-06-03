package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.api.Coord4D;
import mekanism.common.IUpgradeManagement;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityBasicBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class PacketRemoveUpgrade extends MekanismPacket
{
	public Coord4D coord4D;

	public byte upgradeType;
	
	public PacketRemoveUpgrade() {}

	public PacketRemoveUpgrade(Coord4D coord, byte type)
	{
		coord4D = coord;
		upgradeType = type;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);

		dataStream.writeByte(upgradeType);
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();

		byte type = dataStream.readByte();

		TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);

		if(tileEntity instanceof IUpgradeManagement && tileEntity instanceof TileEntityBasicBlock)
		{
			IUpgradeManagement upgradeTile = (IUpgradeManagement)tileEntity;

			if(type == 0)
			{
				if(upgradeTile.getSpeedMultiplier() > 0)
				{
					if(player.inventory.addItemStackToInventory(new ItemStack(Mekanism.SpeedUpgrade)))
					{
						upgradeTile.setSpeedMultiplier(upgradeTile.getSpeedMultiplier()-1);
					}
				}
			}
			else if(type == 1)
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
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{

	}
}
