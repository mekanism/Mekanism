package mekanism.common.tile;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Range4D;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;

import cofh.api.energy.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

public class TileEntityChargepad extends TileEntityNoisyElectricBlock
{
	public boolean isActive;

	public boolean prevActive;

	public Random random = new Random();

	public TileEntityChargepad()
	{
		super("machine.chargepad", "Chargepad", MachineType.CHARGEPAD.baseEnergy);
		inventory = new ItemStack[0];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			isActive = false;
			List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 0.2, zCoord + 1));

			for(EntityLivingBase entity : entities)
			{
				if(entity instanceof EntityPlayer || entity instanceof EntityRobit)
				{
					isActive = true;
				}

				if(getEnergy() > 0)
				{
					if(entity instanceof EntityRobit)
					{
						EntityRobit robit = (EntityRobit) entity;

						double canGive = Math.min(getEnergy(), 1000);
						double toGive = Math.min(robit.MAX_ELECTRICITY - robit.getEnergy(), canGive);

						robit.setEnergy(robit.getEnergy() + toGive);
						setEnergy(getEnergy() - toGive);
					} 
					else if(entity instanceof EntityPlayer)
					{
						EntityPlayer player = (EntityPlayer) entity;

						double prevEnergy = getEnergy();

						for(ItemStack itemstack : player.inventory.armorInventory)
						{
							chargeItemStack(itemstack);

							if(prevEnergy != getEnergy())
							{
								break;
							}
						}

						for(ItemStack itemstack : player.inventory.mainInventory)
						{
							chargeItemStack(itemstack);

							if(prevEnergy != getEnergy())
							{
								break;
							}
						}
					}
				}
			}

			if(prevActive != isActive)
			{
				worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.1, zCoord + 0.5, "random.click", 0.3F, isActive ? 0.6F : 0.5F);
				setActive(isActive);
			}
		}
		else if(isActive)
		{
			worldObj.spawnParticle("reddust", xCoord+random.nextDouble(), yCoord+0.15, zCoord+random.nextDouble(), 0, 0, 0);
		}
	}

	public void chargeItemStack(ItemStack itemstack)
	{
		if(itemstack != null)
		{
			if(itemstack.getItem() instanceof IEnergizedItem)
			{
				setEnergy(getEnergy() - EnergizedItemManager.charge(itemstack, getEnergy()));
			}
			else if(MekanismUtils.useIC2() && itemstack.getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.manager.charge(itemstack, (int)(getEnergy()*general.TO_IC2), 4, true, false)*general.FROM_IC2;
				setEnergy(getEnergy() - sent);
			}
			else if(MekanismUtils.useRF() && itemstack.getItem() instanceof IEnergyContainerItem)
			{
				IEnergyContainerItem item = (IEnergyContainerItem)itemstack.getItem();

				int itemEnergy = (int)Math.round(Math.min(Math.sqrt(item.getMaxEnergyStored(itemstack)), item.getMaxEnergyStored(itemstack) - item.getEnergyStored(itemstack)));
				int toTransfer = (int)Math.round(Math.min(itemEnergy, (getEnergy()*general.TO_TE)));

				setEnergy(getEnergy() - (item.receiveEnergy(itemstack, toTransfer, false)*general.FROM_TE));
			}
		}
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.of(ForgeDirection.DOWN, ForgeDirection.getOrientation(facing).getOpposite());
	}

	@Override
	public boolean getActive()
	{
		return isActive;
	}

	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(prevActive != active)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
		}

		prevActive = active;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		isActive = nbtTags.getBoolean("isActive");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		isActive = dataStream.readBoolean();
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(isActive);
		return data;
	}

	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getVolume()
	{
		return 0.4F;
	}

	@Override
	public boolean renderUpdate()
	{
		return false;
	}

	@Override
	public boolean lightUpdate()
	{
		return true;
	}
}
