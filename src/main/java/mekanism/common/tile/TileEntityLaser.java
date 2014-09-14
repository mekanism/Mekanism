package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.usage;
import mekanism.common.LaserManager;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class TileEntityLaser extends TileEntityElectricBlock
{
	public boolean on;

	public TileEntityLaser()
	{
		super("Laser", 2* usage.laserUsage);
		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(worldObj.isRemote)
		{
			if(on)
			{
				LaserManager.fireLaserClient(this, ForgeDirection.getOrientation(facing), usage.laserUsage, worldObj);
			}
		}
		else
		{
			if(getEnergy() >= usage.laserUsage)
			{
				if(!on)
				{
					on = true;
					Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
				}
				
				LaserManager.fireLaser(this, ForgeDirection.getOrientation(facing), usage.laserUsage, worldObj);
				setEnergy(getEnergy() - usage.laserUsage);
			}
			else if(on)
			{
				on = false;
				Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
			}
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(on);

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		on = dataStream.readBoolean();
	}
}
