package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.lasers.LaserManager;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class TileEntityLaser extends TileEntityElectricBlock
{
	public static final double LASER_ENERGY = 1E10;

	public boolean on;

	public TileEntityLaser()
	{
		super("Laser", 2*LASER_ENERGY);
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
				LaserManager.fireLaserClient(Coord4D.get(this), ForgeDirection.getOrientation(facing), worldObj);
			}
		}
		else
		{
			if(getEnergy() >= LASER_ENERGY)
			{
				if(!on)
				{
					on = true;
					Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
				}
				
				LaserManager.fireLaser(Coord4D.get(this), ForgeDirection.getOrientation(facing), LASER_ENERGY, worldObj);
				setEnergy(getEnergy() - LASER_ENERGY);
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
