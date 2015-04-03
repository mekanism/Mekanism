package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.LaserManager;
import mekanism.common.LaserManager.LaserInfo;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityLaserAmplifier extends TileEntityContainerBlock implements ILaserReceptor, IRedstoneControl, ICableOutputter, IStrictEnergyStorage
{
	public static final double MAX_ENERGY = 5E9;
	public double collectedEnergy = 0;
	public double lastFired = 0;

	public double minThreshold = 0;
	public double maxThreshold = 5E9;
	public int ticks = 0;
	public int time = 0;

	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public boolean on = false;

	public Coord4D digging;
	public double diggingProgress;
	
	public boolean emittingRedstone;
	public boolean entityDetection;

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
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			if(on)
			{
				MovingObjectPosition mop = LaserManager.fireLaserClient(this, ForgeDirection.getOrientation(facing), lastFired, worldObj);
				Coord4D hitCoord = mop == null ? null : new Coord4D(mop.blockX, mop.blockY, mop.blockZ);

				if(hitCoord == null || !hitCoord.equals(digging))
				{
					digging = hitCoord;
					diggingProgress = 0;
				}

				if(hitCoord != null)
				{
					Block blockHit = hitCoord.getBlock(worldObj);
					TileEntity tileHit = hitCoord.getTileEntity(worldObj);
					float hardness = blockHit.getBlockHardness(worldObj, hitCoord.xCoord, hitCoord.yCoord, hitCoord.zCoord);

					if(!(hardness < 0 || (tileHit instanceof ILaserReceptor && !((ILaserReceptor)tileHit).canLasersDig())))
					{
						diggingProgress += lastFired;

						if(diggingProgress < hardness*general.laserEnergyNeededPerHardness)
						{
							Mekanism.proxy.addHitEffects(hitCoord, mop);
						}
					}
				}

			}
		}
		else {
			boolean prevRedstone = emittingRedstone;
			
			emittingRedstone = false;
			
			if(ticks < time)
			{
				ticks++;
			}
			else {
				ticks = 0;
			}

			if(toFire() > 0)
			{
				double firing = toFire();

				if(!on || firing != lastFired)
				{
					on = true;
					lastFired = firing;
					Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
				}

				LaserInfo info = LaserManager.fireLaser(this, ForgeDirection.getOrientation(facing), firing, worldObj);
				Coord4D hitCoord = info.movingPos == null ? null : new Coord4D(info.movingPos.blockX, info.movingPos.blockY, info.movingPos.blockZ);

				if(hitCoord == null || !hitCoord.equals(digging))
				{
					digging = hitCoord;
					diggingProgress = 0;
				}

				if(hitCoord != null)
				{
					Block blockHit = hitCoord.getBlock(worldObj);
					TileEntity tileHit = hitCoord.getTileEntity(worldObj);
					float hardness = blockHit.getBlockHardness(worldObj, hitCoord.xCoord, hitCoord.yCoord, hitCoord.zCoord);
					
					if(!(hardness < 0 || (tileHit instanceof ILaserReceptor && !((ILaserReceptor)tileHit).canLasersDig())))
					{
						diggingProgress += firing;

						if(diggingProgress >= hardness*general.laserEnergyNeededPerHardness)
						{
							LaserManager.breakBlock(hitCoord, true, worldObj);
							diggingProgress = 0;
						}
					}
				}
				
				emittingRedstone = info.foundEntity;

				setEnergy(getEnergy() - firing);
			}
			else if(on)
			{
				on = false;
				diggingProgress = 0;
				Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
			}
			
			if(!entityDetection)
			{
				emittingRedstone = false;
			}
			
			if(emittingRedstone != prevRedstone)
			{
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
			}
		}
	}

	@Override
	public void setEnergy(double energy)
	{
		collectedEnergy = Math.max(0, Math.min(energy, MAX_ENERGY));
	}

	@Override
	public double getEnergy()
	{
		return collectedEnergy;
	}

	public boolean shouldFire()
	{
		return collectedEnergy >= minThreshold && ticks >= time && MekanismUtils.canFunction(this);
	}

	public double toFire()
	{
		return shouldFire() ? Math.min(collectedEnergy, maxThreshold) : 0;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(on);
		data.add(minThreshold);
		data.add(maxThreshold);
		data.add(time);
		data.add(collectedEnergy);
		data.add(lastFired);
		data.add(controlType.ordinal());
		data.add(emittingRedstone);
		data.add(entityDetection);

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(worldObj.isRemote)
		{
			super.handlePacketData(dataStream);

			on = dataStream.readBoolean();

			minThreshold = dataStream.readDouble();
			maxThreshold = dataStream.readDouble();
			time = dataStream.readInt();
			collectedEnergy = dataStream.readDouble();
			lastFired = dataStream.readDouble();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			emittingRedstone = dataStream.readBoolean();
			entityDetection = dataStream.readBoolean();

			return;
		}

		switch(dataStream.readInt())
		{
			case 0:
				minThreshold = Math.min(MAX_ENERGY, MekanismUtils.convertToJoules(dataStream.readDouble()));
				break;
			case 1:
				maxThreshold = Math.min(MAX_ENERGY, MekanismUtils.convertToJoules(dataStream.readDouble()));
				break;
			case 2:
				time = dataStream.readInt();
				break;
			case 3:
				entityDetection = !entityDetection;
				break;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		on = nbtTags.getBoolean("on");
		
		minThreshold = nbtTags.getDouble("minThreshold");
		maxThreshold = nbtTags.getDouble("maxThreshold");
		time = nbtTags.getInteger("time");
		collectedEnergy = nbtTags.getDouble("collectedEnergy");
		lastFired = nbtTags.getDouble("lastFired");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
		entityDetection = nbtTags.getBoolean("entityDetection");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("on", on);
		
		nbtTags.setDouble("minThreshold", minThreshold);
		nbtTags.setDouble("maxThreshold", maxThreshold);
		nbtTags.setInteger("time", time);
		nbtTags.setDouble("collectedEnergy", collectedEnergy);
		nbtTags.setDouble("lastFired", lastFired);
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setBoolean("entityDetection", entityDetection);
	}

	@Override
	public RedstoneControl getControlType()
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type)
	{
		controlType = type;
	}

	@Override
	public boolean canPulse()
	{
		return true;
	}

	@Override
	public boolean canOutputTo(ForgeDirection side)
	{
		return true;
	}

	@Override
	public double getMaxEnergy()
	{
		return MAX_ENERGY;
	}
}
