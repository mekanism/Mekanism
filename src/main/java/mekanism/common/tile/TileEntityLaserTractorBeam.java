package mekanism.common.tile;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.util.StackUtils;
import mekanism.common.LaserManager;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.InventoryUtils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.EnumFacing;

import io.netty.buffer.ByteBuf;

public class TileEntityLaserTractorBeam extends TileEntityContainerBlock implements ILaserReceptor
{
	public static final double MAX_ENERGY = 5E9;
	public double collectedEnergy = 0;
	public double lastFired = 0;

	public boolean on = false;

	public Coord4D digging;
	public double diggingProgress;

	public static int[] availableSlotIDs = InventoryUtils.getIntRange(0, 26);

	public TileEntityLaserTractorBeam()
	{
		super("LaserTractorBeam");
		inventory = new ItemStack[27];
	}

	@Override
	public void receiveLaserEnergy(double energy, EnumFacing side)
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
				LaserManager.fireLaserClient(this, getFacing(), lastFired, worldObj);
			}
		}
		else
		{
			if(collectedEnergy > 0)
			{
				double firing = collectedEnergy;

				if(!on || firing != lastFired)
				{
					on = true;
					lastFired = firing;
					Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(50D));
				}

				MovingObjectPosition mop = LaserManager.fireLaser(this, getFacing(), firing, worldObj);
				Coord4D hitCoord = mop == null ? null : new Coord4D(mop.getBlockPos());

				if(hitCoord == null || !hitCoord.equals(digging))
				{
					digging = hitCoord;
					diggingProgress = 0;
				}

				if(hitCoord != null)
				{
					Block blockHit = hitCoord.getBlock(worldObj);
					TileEntity tileHit = hitCoord.getTileEntity(worldObj);
					float hardness = blockHit.getBlockHardness(worldObj, hitCoord);
					if(!(hardness < 0 || (tileHit instanceof ILaserReceptor && !((ILaserReceptor)tileHit).canLasersDig())))
					{
						diggingProgress += firing;

						if(diggingProgress >= hardness * general.laserEnergyNeededPerHardness)
						{
							List<ItemStack> drops = LaserManager.breakBlock(hitCoord, false, worldObj);
							if(drops != null) receiveDrops(drops);
							diggingProgress = 0;
						}
						else
						{
							Minecraft.getMinecraft().effectRenderer.addBlockHitEffects(hitCoord, mop);
						}
					}
				}

				setEnergy(getEnergy() - firing);
			}
			else if(on)
			{
				on = false;
				diggingProgress = 0;
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

	public void receiveDrops(List<ItemStack> drops)
	{
		outer:
		for(ItemStack drop : drops)
		{
			for(int i = 0; i < inventory.length; i++)
			{
				if(inventory[i] == null)
				{
					inventory[i] = drop;
					continue outer;
				}
				ItemStack slot = inventory[i];
				if(StackUtils.equalsWildcardWithNBT(slot, drop))
				{
					int change = Math.min(drop.stackSize, slot.getMaxStackSize() - slot.stackSize);
					slot.stackSize += change;
					drop.stackSize -= change;
					if(drop.stackSize <= 0) continue outer;
				}
			}
			dropItem(drop);
		}
	}

	public void dropItem(ItemStack stack)
	{
		EntityItem item = new EntityItem(worldObj, getPos().getX() + 0.5, getPos().getY() + 1, getPos().getZ() + 0.5, stack);
		item.motionX = worldObj.rand.nextGaussian() * 0.05;
		item.motionY = worldObj.rand.nextGaussian() * 0.05 + 0.2;
		item.motionZ = worldObj.rand.nextGaussian() * 0.05;
		item.setPickupDelay(10);
		worldObj.spawnEntityInWorld(item);
	}

	public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_)
	{
		return false;
	}

	public int[] getSlotsForFace(EnumFacing p_94128_1_)
	{
		return availableSlotIDs;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(on);
		data.add(collectedEnergy);
		data.add(lastFired);

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(worldObj.isRemote)
		{
			super.handlePacketData(dataStream);

			on = dataStream.readBoolean();
			collectedEnergy = dataStream.readDouble();
			lastFired = dataStream.readDouble();

			return;
		}
	}
}
