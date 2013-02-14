package mekanism.generators.common;

import java.util.List;

import ic2.api.ICustomElectricItem;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricInfo.ElectricUnit;
import universalelectricity.core.implement.IItemElectric;
import mekanism.common.Mekanism;
import mekanism.common.TileEntityElectricBlock;
import mekanism.generators.common.BlockGenerator.GeneratorType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.world.World;

/**
 * Item class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 2: Electrolytic Separator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
 * 5: Advanced Solar Generator
 * 6: Hydro Generator
 * @author AidanBrady
 *
 */
public class ItemBlockGenerator extends ItemBlock implements IItemElectric, ICustomElectricItem
{
	public Block metaBlock;
	
	public ItemBlockGenerator(int id, Block block)
	{
		super(id);
		metaBlock = block;
		setHasSubtypes(true);
		setMaxStackSize(1);
	}
	
	@Override
	public int getMetadata(int i)
	{
		return i;
	}
	
	@Override
	public int getIconFromDamage(int i)
	{
		return metaBlock.getBlockTextureFromSideAndMetadata(2, i);
	}
	
	@Override
	public String getItemNameIS(ItemStack itemstack)
	{
		String name = "";
		switch(itemstack.getItemDamage())
		{
			case 0:
				name = "HeatGenerator";
				break;
			case 1:
				name = "SolarGenerator";
				break;
			case 2:
				name = "ElectrolyticSeparator";
				break;
			case 3:
				name = "HydrogenGenerator";
				break;
			case 4:
				name = "BioGenerator";
				break;
			case 5:
				name = "AdvancedSolarGenerator";
				break;
			case 6:
				name = "HydroGenerator";
				break;
			default:
				name = "Unknown";
				break;
		}
		return getItemName() + "." + name;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		double energy = getJoules(itemstack);
		
		list.add("Stored Energy: " + ElectricInfo.getDisplayShort(energy, ElectricUnit.JOULES));
	}
	
	@Override
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
    {
    	ItemBlockGenerator item = ((ItemBlockGenerator)itemstack.getItem());
    	item.setJoules(item.getJoules(itemstack), itemstack);
    }

	@Override
	public double getJoules(Object... data)
	{
		if (data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack) data[0];

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
			
			return electricityStored;
		}

		return -1;
	}

	@Override
	public void setJoules(double wattHours, Object... data)
	{
		if (data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if (itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			double electricityStored = Math.max(Math.min(wattHours, getMaxJoules(itemStack)), 0);
			itemStack.stackTagCompound.setDouble("electricity", electricityStored);
		}
	}

	@Override
	public double getMaxJoules(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
			return GeneratorType.getFromMetadata(itemstack.getItemDamage()).maxEnergy;
		}
		
		return 3200;
	}

	@Override
	public double getVoltage(Object... data) 
	{
		return 120;
	}

	@Override
	public double onReceive(double amps, double voltage, ItemStack itemStack)
	{
		double rejectedElectricity = Math.max((getJoules(itemStack) + ElectricInfo.getJoules(amps, voltage, 1)) - getMaxJoules(itemStack), 0);
		setJoules(getJoules(itemStack) + ElectricInfo.getJoules(amps, voltage, 1) - rejectedElectricity, itemStack);
		return rejectedElectricity;
	}

	@Override
	public double onUse(double joulesNeeded, ItemStack itemStack)
	{
		double electricityToUse = Math.min(getJoules(itemStack), joulesNeeded);
		setJoules(getJoules(itemStack) - electricityToUse, itemStack);
		return electricityToUse;
	}

	@Override
	public boolean canReceiveElectricity()
	{
		//TODO
		return false;
	}

	@Override
	public boolean canProduceElectricity()
	{
		return true;
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		boolean place = true;
		
		if(stack.getItemDamage() == GeneratorType.ADVANCED_SOLAR_GENERATOR.meta)
		{
	        if(world.getBlockId(x, y, z) != Block.tallGrass.blockID && world.getBlockId(x, y, z) != 0) 
	        	place = false;
	        
	        if(world.getBlockId(x, y, z) != 0)
	        {
	        	if(Block.blocksList[world.getBlockId(x, y, z)].isBlockReplaceable(world, x, y, z)) 
	        		place = true; 
	        }
	        
			for(int xPos=-1;xPos<=1;xPos++)
			{
				for(int zPos=-1;zPos<=1;zPos++)
				{
					if(world.getBlockId(x+xPos, y+2, z+zPos) != 0) 
						place = false;
				}
			}
		}
		
		if(place && super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
		{
    		TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    		tileEntity.electricityStored = getJoules(stack);
    		return true;
		}
		
		return false;
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
		return canProduceElectricity();
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
		return (int)(getVoltage()*Mekanism.TO_IC2);
	}
}
