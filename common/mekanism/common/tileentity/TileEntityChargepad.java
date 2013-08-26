package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import mekanism.api.Object3D;
import mekanism.api.energy.EnergizedItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.client.sound.IHasSound;
import mekanism.common.EntityRobit;
import mekanism.common.IActiveState;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import thermalexpansion.api.item.IChargeableItem;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityChargepad extends TileEntityElectricBlock implements IActiveState, IEnergySink, IStrictEnergyAcceptor, IHasSound
{
	public boolean isActive;
	
	public boolean prevActive;
	
	public Random random = new Random();
	
	public TileEntityChargepad()
	{
		super("Chargepad", 9000);
		inventory = new ItemStack[0];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			isActive = false;
			
			List<EntityLiving> entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+0.2, zCoord+1));
			
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
						EntityRobit robit = (EntityRobit)entity;
						
						double canGive = Math.min(getEnergy(), 1000);
						double toGive = Math.min(robit.MAX_ELECTRICITY-robit.getEnergy(), canGive);
						
						robit.setEnergy(robit.getEnergy() + toGive);
						setEnergy(getEnergy() - toGive);
					}
					else if(entity instanceof EntityPlayer)
					{
						EntityPlayer player = (EntityPlayer)entity;
						
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
		else {
			Mekanism.proxy.registerSound(this);
			
			if(isActive)
			{
				worldObj.spawnParticle("reddust", xCoord+random.nextDouble(), yCoord+0.15, zCoord+random.nextDouble(), 0, 0, 0);
			}
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
			else if(itemstack.getItem() instanceof IItemElectric)
			{
				setEnergy(getEnergy() - ElectricItemHelper.chargeItem(itemstack, (float)getEnergy()));
			}
			else if(Mekanism.hooks.IC2Loaded && itemstack.getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.manager.charge(itemstack, (int)(getEnergy()*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
				setEnergy(getEnergy() - sent);
			}
			else if(itemstack.getItem() instanceof IChargeableItem)
			{
				IChargeableItem item = (IChargeableItem)itemstack.getItem();
				
				float itemEnergy = (float)Math.min(Math.sqrt(item.getMaxEnergyStored(itemstack)), item.getMaxEnergyStored(itemstack) - item.getEnergyStored(itemstack));
				float toTransfer = (float)Math.min(itemEnergy, (getEnergy()*Mekanism.TO_BC));
				
				item.receiveEnergy(itemstack, toTransfer, true);
				setEnergy(getEnergy() - (toTransfer*Mekanism.FROM_BC));
			}
		}
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(worldObj.isRemote)
		{
			Mekanism.proxy.unregisterSound(this);
		}
	}
	
	@Override
	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.of(ForgeDirection.DOWN, ForgeDirection.getOrientation(facing));
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
    		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
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
	public void handlePacketData(ByteArrayDataInput dataStream)
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
	public double transferEnergyToAcceptor(double amount)
	{
    	double rejects = 0;
    	double neededElectricity = getMaxEnergy()-getEnergy();
    	
    	if(amount <= neededElectricity)
    	{
    		electricityStored += amount;
    	}
    	else {
    		electricityStored += neededElectricity;
    		rejects = amount-neededElectricity;
    	}
    	
    	return rejects;
	}
	
	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return side == ForgeDirection.DOWN || side == ForgeDirection.getOrientation(facing).getOpposite();
	}
	
	@Override
	public double demandedEnergyUnits() 
	{
		return (getMaxEnergy()-getEnergy())*Mekanism.TO_IC2;
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = getMaxEnergy()-getEnergy();
    	
    	if(givenEnergy <= neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return rejects*Mekanism.TO_IC2;
    }
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return direction == ForgeDirection.DOWN || direction == ForgeDirection.getOrientation(facing).getOpposite();
	}

	@Override
	public String getSoundPath() 
	{
		return "Chargepad.ogg";
	}

	@Override
	public float getVolumeMultiplier() 
	{
		return 0.7F;
	}
	
	@Override
	public boolean hasVisual()
	{
		return true;
	}
}
