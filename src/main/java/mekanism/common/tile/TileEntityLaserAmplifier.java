package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.lasers.LaserManager;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class TileEntityLaserAmplifier extends TileEntityContainerBlock implements ILaserReceptor
{
	public static final double MAX_ENERGY = 5E9;
	public double collectedEnergy = 0;

	public double threshold = 0;
	public int ticks = 0;
	public int time = 0;

	public LaserEmitterMode mode = LaserEmitterMode.THRESHOLD;
	public boolean poweredNow = false;
	public boolean poweredLastTick = false;
	public boolean on = false;

	public TileEntityLaserAmplifier()
	{
		super("LaserAmplifier");
		inventory = new ItemStack[0];
	}

	@Override
	public void receiveLaserEnergy(double energy, ForgeDirection side)
	{
		setEnergy(getEnergy() + energy);
	}

	@Override
	public boolean canLasersDig()
	{
		return false;
	}

	@Override
	public double energyToDig()
	{
		return Double.MAX_VALUE;
	}

	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			if(on)
			{
				LaserManager.fireLaserClient(Coord4D.get(this), ForgeDirection.getOrientation(facing), worldObj);
			}
		}
		else
		{
			poweredNow = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

			if(ticks < time)
			{
				ticks++;
			}
			else
			{
				ticks = 0;
			}

			if(shouldFire() && toFire() > 0)
			{
				if(!on)
				{
					on = true;
					Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
				}

				LaserManager.fireLaser(Coord4D.get(this), ForgeDirection.getOrientation(facing), toFire(), worldObj);
				setEnergy(getEnergy() - toFire());
			}
			else if(on)
			{
				on = false;
				Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
			}

			poweredLastTick = poweredNow;
		}
	}

	public void setEnergy(double energy)
	{
		collectedEnergy = Math.max(0, Math.min(energy, MAX_ENERGY));
	}

	public double getEnergy()
	{
		return collectedEnergy;
	}

	public boolean shouldFire()
	{
		switch(mode)
		{
			case THRESHOLD:
				return collectedEnergy >= threshold;
			case REDSTONE:
				return poweredNow;
			case REDSTONE_PULSE:
				return poweredNow && !poweredLastTick;
			case TIMER:
				return ticks == time;
		}
		return false;
	}

	public double toFire()
	{
		switch(mode)
		{
			case THRESHOLD:
				return collectedEnergy;
			case REDSTONE:
				return collectedEnergy;
			case REDSTONE_PULSE:
				return collectedEnergy;
			case TIMER:
				return collectedEnergy;
		}
		return 0;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(on);
		data.add(mode.ordinal());
		data.add(threshold);
		data.add(time);
		data.add(collectedEnergy);

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(worldObj.isRemote)
		{
			super.handlePacketData(dataStream);

			on = dataStream.readBoolean();

			mode = LaserEmitterMode.values()[dataStream.readInt()];

			threshold = dataStream.readDouble();
			time = dataStream.readInt();
			collectedEnergy = dataStream.readDouble();

			return;
		}

		switch(dataStream.readInt())
		{
			case(0):
				mode = LaserEmitterMode.values()[dataStream.readInt()];
				break;
			case(1):
				threshold = dataStream.readDouble();
				break;
			case(2):
				time = dataStream.readInt();
				break;
		}
	}

	public static enum LaserEmitterMode
	{
		THRESHOLD,
		REDSTONE,
		REDSTONE_PULSE,
		TIMER;
	}
}
