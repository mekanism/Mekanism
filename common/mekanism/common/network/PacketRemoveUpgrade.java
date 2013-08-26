package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.IUpgradeManagement;
import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import mekanism.common.tileentity.TileEntityBasicBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketRemoveUpgrade implements IMekanismPacket
{
	public Object3D object3D;
	
	public byte upgradeType;
	
	@Override
	public String getName() 
	{
		return "RemoveUpgrade";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Object3D)data[0];
		upgradeType = (Byte)data[1];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		byte type = dataStream.readByte();
		
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
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
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
		
		dataStream.writeByte(upgradeType);
	}
}
