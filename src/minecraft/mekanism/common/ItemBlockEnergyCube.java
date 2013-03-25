package mekanism.common;

import ic2.api.ICustomElectricItem;

import java.util.ArrayList;
import java.util.List;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;

import mekanism.api.IEnergyCube;
import mekanism.api.Tier.EnergyCubeTier;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.world.World;

public class ItemBlockEnergyCube extends ItemBlock implements IItemElectric, IEnergyCube, ICustomElectricItem
{
	public Block metaBlock;
	
	public ItemBlockEnergyCube(int id, Block block)
	{
		super(id);
		metaBlock = block;
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		double energy = getJoules(itemstack);
		
		list.add("Stored Energy: " + ElectricityDisplay.getDisplayShort(energy, ElectricUnit.JOULES));
		list.add("Voltage: " + getVoltage(itemstack) + "v");
	}
	
	public ItemStack getUnchargedItem(EnergyCubeTier tier)
	{
		ItemStack charged = new ItemStack(this);
		setTier(charged, tier);
		charged.setItemDamage(100);
		return charged;
	}

	@Override
	public double getJoules(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null) 
		{ 
			return 0; 
		}
		
		double electricityStored = 0;
		
		if (itemStack.stackTagCompound.getTag("electricity") instanceof NBTTagFloat)
		{
			electricityStored = itemStack.stackTagCompound.getFloat("electricity");
		}
		else
		{
			electricityStored = itemStack.stackTagCompound.getDouble("electricity");
		}
		
		itemStack.setItemDamage((int)(Math.abs(((electricityStored/getTier(itemStack).MAX_ELECTRICITY)*100)-100)));
		return electricityStored;
	}

	@Override
	public void setJoules(double wattHours, ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(wattHours, getMaxJoules(itemStack)), 0);
		itemStack.stackTagCompound.setDouble("electricity", electricityStored);
		itemStack.setItemDamage((int)(Math.abs(((electricityStored/getTier(itemStack).MAX_ELECTRICITY)*100)-100)));
	}

	@Override
	public double getMaxJoules(ItemStack itemStack)
	{
		return getTier(itemStack).MAX_ELECTRICITY;
	}

	@Override
	public double getVoltage(ItemStack itemStack) 
	{
		return getTier(itemStack).VOLTAGE;
	}

	@Override
	public ElectricityPack onReceive(ElectricityPack electricityPack, ItemStack itemStack)
	{
		double rejectedElectricity = Math.max((getJoules(itemStack) + electricityPack.getWatts()) - getMaxJoules(itemStack), 0);
		double joulesToStore = electricityPack.getWatts() - rejectedElectricity;
		this.setJoules(getJoules(itemStack) + joulesToStore, itemStack);
		return ElectricityPack.getFromWatts(joulesToStore, getVoltage(itemStack));
	}

	@Override
	public ElectricityPack onProvide(ElectricityPack electricityPack, ItemStack itemStack)
	{
		double electricityToUse = Math.min(getJoules(itemStack), electricityPack.getWatts());
		setJoules(getJoules(itemStack) - electricityToUse, itemStack);
		return ElectricityPack.getFromWatts(electricityToUse, getVoltage(itemStack));
	}

	@Override
	public ElectricityPack getReceiveRequest(ItemStack itemStack)
	{
		return ElectricityPack.getFromWatts(Math.min(getMaxJoules(itemStack) - getJoules(itemStack), getTransferRate(itemStack)), getVoltage(itemStack));
	}

	@Override
	public ElectricityPack getProvideRequest(ItemStack itemStack)
	{
		return ElectricityPack.getFromWatts(Math.min(getJoules(itemStack), getTransferRate(itemStack)), getVoltage(itemStack));
	}
	
	public double getTransferRate(ItemStack itemStack)
	{
		return getMaxJoules(itemStack)*0.01;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		return getUnlocalizedName() + "." + getTier(itemstack).name;
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
    	boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    	
    	if(place)
    	{
    		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);
    		tileEntity.tier = ((IEnergyCube)stack.getItem()).getTier(stack);
    		tileEntity.electricityStored = getJoules(stack);
    		
    		if(tileEntity.powerProvider != null)
    		{
    			tileEntity.powerProvider.configure(0, 0, 100, 0, (int)(tileEntity.tier.MAX_ELECTRICITY*Mekanism.TO_BC));
    		}
    		
    		if(!world.isRemote)
    		{
    			PacketHandler.sendTileEntityPacketToClients(tileEntity, 0, tileEntity.getNetworkedData(new ArrayList()));
    		}
    	}
    	
    	return place;
    }

	@Override
	public EnergyCubeTier getTier(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{ 
			return EnergyCubeTier.BASIC; 
		}
		
		if(itemstack.stackTagCompound.getString("tier") == null)
		{
			return EnergyCubeTier.BASIC;
		}
		
		return EnergyCubeTier.getFromName(itemstack.stackTagCompound.getString("tier"));
	}

	@Override
	public void setTier(ItemStack itemstack, EnergyCubeTier tier) 
	{
		if (itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setString("tier", tier.name);
	}
	
	@Override
	public int charge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		double givenEnergy = amount*Mekanism.FROM_IC2;
		double energyNeeded = getTier(itemStack).MAX_ELECTRICITY-getJoules(itemStack);
		double energyToStore = Math.min(Math.min(amount, getTier(itemStack).MAX_ELECTRICITY*0.01), energyNeeded);
		
		if(!simulate)
		{
			setJoules(getJoules(itemStack) + energyToStore, itemStack);
		}
		return (int)(energyToStore*Mekanism.TO_IC2);
	}
	
	@Override
	public int discharge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		double energyWanted = amount*Mekanism.FROM_IC2;
		double energyToGive = Math.min(Math.min(energyWanted, getTier(itemStack).MAX_ELECTRICITY*0.01), getJoules(itemStack));
		
		if(!simulate)
		{
			setJoules(getJoules(itemStack) - energyToGive, itemStack);
		}
		return (int)(energyToGive*Mekanism.TO_IC2);
	}

	@Override
	public boolean canUse(ItemStack itemStack, int amount)
	{
		return getJoules(itemStack) >= amount*Mekanism.FROM_IC2;
	}
	
	@Override
	public boolean canShowChargeToolTip(ItemStack itemStack)
	{
		return false;
	}
	
	@Override
	public boolean canProvideEnergy()
	{
		return true;
	}

	@Override
	public int getChargedItemId()
	{
		return itemID;
	}

	@Override
	public int getEmptyItemId()
	{
		return itemID;
	}

	@Override
	public int getMaxCharge()
	{
		return 0;
	}

	@Override
	public int getTier()
	{
		return 3;
	}

	@Override
	public int getTransferLimit()
	{
		return 0;
	}
}
