package mekanism.common;

import java.util.List;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.item.IItemElectric;

import ic2.api.ICustomElectricItem;
import mekanism.api.IUpgradeManagement;
import mekanism.common.BlockMachine.MachineType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.world.World;

/**
 * Item class for handling multiple machine block IDs.
 * 0: Enrichment Chamber
 * 1: Osmium Compressor
 * 2: Combiner
 * 3: Crusher
 * 4: Theoretical Elementizer
 * 5: Basic Factory
 * 6: Advanced Factory
 * 7: Elite Factory
 * 8: Metallurgic Infuser
 * 9: Purification Chamber
 * 10: Energized Smelter
 * @author AidanBrady
 *
 */
public class ItemBlockMachine extends ItemBlock implements IItemElectric, ICustomElectricItem, IUpgradeManagement, IFactory
{
	public Block metaBlock;
	
	public ItemBlockMachine(int id, Block block)
	{
		super(id);
		metaBlock = block;
		setHasSubtypes(true);
		setNoRepair();
		setMaxStackSize(1);
	}
	
	@Override
	public int getMetadata(int i)
	{
		return i;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String name = "";
		switch(itemstack.getItemDamage())
		{
			case 0:
				name = "EnrichmentChamber";
				break;
			case 1:
				name = "OsmiumCompressor";
				break;
			case 2:
				name = "Combiner";
				break;
			case 3:
				name = "Crusher";
				break;
			case 4:
				name = "TheoreticalElementizer";
				break;
			case 5:
				name = "BasicFactory";
				break;
			case 6:
				name = "AdvancedFactory";
				break;
			case 7:
				name = "EliteFactory";
				break;
			case 8:
				name = "MetallurgicInfuser";
				break;
			case 9:
				name = "PurificationChamber";
				break;
			case 10:
				name = "EnergizedSmelter";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getUnlocalizedName() + "." + name;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		double energy = getJoules(itemstack);
		
		if(isFactory(itemstack))
		{
			list.add("Recipe Type: " + RecipeType.values()[getRecipeType(itemstack)].getName());
		}
		
		list.add("Stored Energy: " + ElectricityDisplay.getDisplayShort(energy, ElectricUnit.JOULES));
		list.add("Energy: x" + (getEnergyMultiplier(itemstack)+1));
		list.add("Speed: x" + (getSpeedMultiplier(itemstack)+1));
	}

	@Override
	public double getJoules(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null) 
		{ 
			return 0; 
		}
		
		double electricityStored = 0;
		
		if(itemStack.stackTagCompound.getTag("electricity") instanceof NBTTagFloat)
		{
			electricityStored = itemStack.stackTagCompound.getFloat("electricity");
		}
		else {
			electricityStored = itemStack.stackTagCompound.getDouble("electricity");
		}
		
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
	}

	@Override
	public double getMaxJoules(ItemStack itemStack)
	{
		return MekanismUtils.getEnergy(getEnergyMultiplier(itemStack), MachineType.getFromMetadata(itemStack.getItemDamage()).baseEnergy);
	}

	@Override
	public double getVoltage(ItemStack itemStack) 
	{
		return 120;
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
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
    	boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    	
    	if(place)
    	{
    		TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    		
    		if(tileEntity instanceof IUpgradeManagement)
    		{
    			((IUpgradeManagement)tileEntity).setEnergyMultiplier(getEnergyMultiplier(stack));
    			((IUpgradeManagement)tileEntity).setSpeedMultiplier(getSpeedMultiplier(stack));
    		}
    		
    		if(tileEntity instanceof TileEntityFactory)
    		{
    			((TileEntityFactory)tileEntity).recipeType = getRecipeType(stack);
    		}
    		
    		tileEntity.electricityStored = getJoules(stack);
    	}
    	
    	return place;
    }
	
	@Override
	public int charge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		double givenEnergy = amount*Mekanism.FROM_IC2;
		double energyNeeded = getMaxJoules(itemStack)-getJoules(itemStack);
		double energyToStore = Math.min(Math.min(amount, getMaxJoules(itemStack)*0.01), energyNeeded);
		
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
		double energyToGive = Math.min(Math.min(energyWanted, getMaxJoules(itemStack)*0.01), getJoules(itemStack));
		
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
		return false;
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

	@Override
	public int getEnergyMultiplier(Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack) data[0];

			if (itemStack.stackTagCompound == null) 
			{ 
				return 0; 
			}
			
			return itemStack.stackTagCompound.getInteger("energyMultiplier");
		}

		return 0;
	}

	@Override
	public void setEnergyMultiplier(int multiplier, Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if (itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.stackTagCompound.setInteger("energyMultiplier", multiplier);
		}
	}

	@Override
	public int getSpeedMultiplier(Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack) data[0];

			if(itemStack.stackTagCompound == null) 
			{ 
				return 0; 
			}
			
			return itemStack.stackTagCompound.getInteger("speedMultiplier");
		}

		return 0;
	}

	@Override
	public void setSpeedMultiplier(int multiplier, Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.stackTagCompound.setInteger("speedMultiplier", multiplier);
		}
	}

	@Override
	public int getRecipeType(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null) 
		{ 
			return 0; 
		}
		
		return itemStack.stackTagCompound.getInteger("recipeType");
	}

	@Override
	public void setRecipeType(int type, ItemStack itemStack) 
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setInteger("recipeType", type);
	}
	
	@Override
	public boolean isFactory(ItemStack itemStack)
	{
		return itemStack.getItem() instanceof ItemBlockMachine && itemStack.getItemDamage() >= 5 && itemStack.getItemDamage() <= 7;
	}
}
