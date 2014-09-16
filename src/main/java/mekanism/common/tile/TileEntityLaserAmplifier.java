package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.LaserManager;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class TileEntityLaserAmplifier extends TileEntityContainerBlock implements ILaserReceptor, IRedstoneControl
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
				LaserManager.fireLaserClient(this, ForgeDirection.getOrientation(facing), lastFired, worldObj);
			}
		}
		else
		{
			if(ticks < time)
			{
				ticks++;
			}
			else
			{
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

				MovingObjectPosition mop = LaserManager.fireLaser(this, ForgeDirection.getOrientation(facing), firing, worldObj);
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
						diggingProgress += firing;

						if(diggingProgress >= hardness * general.laserEnergyNeededPerHardness)
						{
							LaserManager.breakBlock(hitCoord, true, worldObj);
							diggingProgress = 0;
						}
						else
						{
							Minecraft.getMinecraft().effectRenderer.addBlockHitEffects(hitCoord.xCoord, hitCoord.yCoord, hitCoord.zCoord, mop);
						}
					}
				}

				setEnergy(getEnergy() - firing);
			}
			else if(on)
			{
				on = false;
				Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
			}
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

			return;
		}

		switch(dataStream.readInt())
		{
			case 0:
				minThreshold = dataStream.readDouble();
				break;
			case 1:
				maxThreshold = dataStream.readDouble();
				break;
			case 2:
				time = dataStream.readInt();
				break;
		}
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
}
