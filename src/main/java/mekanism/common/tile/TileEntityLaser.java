package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.LaserManager;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class TileEntityLaser extends TileEntityElectricBlock
{
	public boolean on;
	public Coord4D digging;
	public double diggingProgress;

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
				
				MovingObjectPosition mop = LaserManager.fireLaser(this, ForgeDirection.getOrientation(facing), usage.laserUsage, worldObj);
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
						diggingProgress += usage.laserUsage;

						if(diggingProgress >= hardness * general.laserEnergyNeededPerHardness)
						{
							blockHit.dropBlockAsItem(worldObj, hitCoord.xCoord, hitCoord.yCoord, hitCoord.zCoord, hitCoord.getMetadata(worldObj), 0);
							blockHit.breakBlock(worldObj, hitCoord.xCoord, hitCoord.yCoord, hitCoord.zCoord, blockHit, hitCoord.getMetadata(worldObj));
							worldObj.setBlockToAir(hitCoord.xCoord, hitCoord.yCoord, hitCoord.zCoord);
							diggingProgress = 0;
							Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(hitCoord.xCoord, hitCoord.yCoord, hitCoord.zCoord, blockHit, hitCoord.getMetadata(worldObj));
						}
						else
						{
							Minecraft.getMinecraft().effectRenderer.addBlockHitEffects(hitCoord.xCoord, hitCoord.yCoord, hitCoord.zCoord, mop);
						}
					}
				}

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
