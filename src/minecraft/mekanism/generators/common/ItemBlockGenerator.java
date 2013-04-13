package mekanism.generators.common;

import java.util.List;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import ic2.api.ICustomElectricItem;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.IItemElectric;
import mekanism.api.EnumColor;
import mekanism.common.ISustainedInventory;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.TileEntityElectricBlock;
import mekanism.generators.common.BlockGenerator.GeneratorType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

/**
 * Item class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 2: Electrolytic Separator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
 * 5: Advanced Solar Generator
 * @author AidanBrady
 *
 */
public class ItemBlockGenerator extends ItemBlock implements IItemElectric, ICustomElectricItem, ISustainedInventory, ISustainedTank
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
	public Icon getIconFromDamage(int i)
	{
		return metaBlock.getIcon(2, i);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
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
			default:
				name = "Unknown";
				break;
		}
		return getUnlocalizedName() + "." + name;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			list.add("Hold " + EnumColor.AQUA + "shift" + EnumColor.GREY + " for details.");
		}
		else {
			list.add(EnumColor.BRIGHT_GREEN + "Stored Energy: " + EnumColor.GREY + ElectricityDisplay.getDisplayShort(getJoules(itemstack), ElectricUnit.JOULES));
			list.add(EnumColor.BRIGHT_GREEN + "Voltage: " + EnumColor.GREY + getVoltage(itemstack) + "v");
			
			if(hasTank(itemstack))
			{
				if(getLiquidStack(itemstack) != null)
				{
					list.add(EnumColor.PINK + LiquidDictionary.findLiquidName(getLiquidStack(itemstack)) + ": " + EnumColor.GREY + getLiquidStack(itemstack).amount + "mB");
				}
			}
			
			list.add(EnumColor.AQUA + "Inventory: " + EnumColor.GREY + (getInventory(itemstack) != null && getInventory(itemstack).tagList != null && !getInventory(itemstack).tagList.isEmpty()));
		}
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
		if (itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(wattHours, getMaxJoules(itemStack)), 0);
		itemStack.stackTagCompound.setDouble("electricity", electricityStored);
	}

	@Override
	public double getMaxJoules(ItemStack itemStack)
	{
		return GeneratorType.getFromMetadata(itemStack.getItemDamage()).maxEnergy;
	}

	@Override
	public double getVoltage(ItemStack itemStack) 
	{
		return itemStack.getItemDamage() == 3 ? 240 : 120;
	}

	@Override
	public ElectricityPack onReceive(ElectricityPack electricityPack, ItemStack itemStack)
	{
		if(itemStack.getItemDamage() == 2)
		{
			double rejectedElectricity = Math.max((getJoules(itemStack) + electricityPack.getWatts()) - getMaxJoules(itemStack), 0);
			double joulesToStore = electricityPack.getWatts() - rejectedElectricity;
			this.setJoules(getJoules(itemStack) + joulesToStore, itemStack);
			return ElectricityPack.getFromWatts(joulesToStore, getVoltage(itemStack));
		}
		
		return new ElectricityPack();
	}

	@Override
	public ElectricityPack onProvide(ElectricityPack electricityPack, ItemStack itemStack)
	{
		if(itemStack.getItemDamage() != 2)
		{
			double electricityToUse = Math.min(getJoules(itemStack), electricityPack.getWatts());
			setJoules(getJoules(itemStack) - electricityToUse, itemStack);
			return ElectricityPack.getFromWatts(electricityToUse, getVoltage(itemStack));
		}
		
		return new ElectricityPack();
	}

	@Override
	public ElectricityPack getReceiveRequest(ItemStack itemStack)
	{
		return itemStack.getItemDamage() == 2 ? ElectricityPack.getFromWatts(Math.min(getMaxJoules(itemStack) - getJoules(itemStack), getTransferRate(itemStack)), getVoltage(itemStack)) : new ElectricityPack();
	}

	@Override
	public ElectricityPack getProvideRequest(ItemStack itemStack)
	{
		return itemStack.getItemDamage() != 2 ? ElectricityPack.getFromWatts(Math.min(getJoules(itemStack), getTransferRate(itemStack)), getVoltage(itemStack)) : new ElectricityPack();
	}
	
	public double getTransferRate(ItemStack itemStack)
	{
		return getMaxJoules(itemStack)*0.01;
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
    		
    		((ISustainedInventory)tileEntity).setInventory(getInventory(stack));
    		
    		if(tileEntity instanceof ISustainedTank)
    		{
    			if(hasTank(stack) && getLiquidStack(stack) != null)
    			{
    				((ISustainedTank)tileEntity).setLiquidStack(getLiquidStack(stack), stack);
    			}
    		}
    		
    		return true;
		}
		
		return false;
    }
	
	@Override
	public int charge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		if(itemStack.getItemDamage() == 2)
		{
			double givenEnergy = amount*Mekanism.FROM_IC2;
			double energyNeeded = getMaxJoules(itemStack)-getJoules(itemStack);
			double energyToStore = Math.min(Math.min(amount, getMaxJoules(itemStack)*0.01), energyNeeded);
			
			if(!simulate)
			{
				setJoules(getJoules(itemStack) + energyToStore, itemStack);
			}
			
			if(energyToStore < 1)
			{
				return 1;
			}
			
			return (int)(energyToStore*Mekanism.TO_IC2);
		}
		
		return 0;
	}
	
	@Override
	public int discharge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		if(itemStack.getItemDamage() != 2)
		{
			double energyWanted = amount*Mekanism.FROM_IC2;
			double energyToGive = Math.min(Math.min(energyWanted, getMaxJoules(itemStack)*0.01), getJoules(itemStack));
			
			if(!simulate)
			{
				setJoules(getJoules(itemStack) - energyToGive, itemStack);
			}
			
			if(energyWanted < 1)
			{
				return 1;
			}
			
			return (int)(energyToGive*Mekanism.TO_IC2);
		}
		
		return 0;
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
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return itemStack.getItemDamage() != 2;
	}

	@Override
	public int getChargedItemId(ItemStack itemStack)
	{
		return itemID;
	}

	@Override
	public int getEmptyItemId(ItemStack itemStack)
	{
		return itemID;
	}

	@Override
	public int getMaxCharge(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public int getTier(ItemStack itemStack)
	{
		return 3;
	}

	@Override
	public int getTransferLimit(ItemStack itemStack)
	{
		return 0;
	}
	
	@Override
	public void setInventory(NBTTagList nbtTags, Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];
			
			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
	
			itemStack.stackTagCompound.setTag("Items", nbtTags);
		}
	}

	@Override
	public NBTTagList getInventory(Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];
			
			if(itemStack.stackTagCompound == null) 
			{ 
				return null; 
			}
			
			return itemStack.stackTagCompound.getTagList("Items");
		}
		
		return null;
	}
	
	@Override
	public void setLiquidStack(LiquidStack liquidStack, Object... data) 
	{
		if(liquidStack == null || liquidStack.amount == 0 || liquidStack.itemID == 0)
		{
			return;
		}
		
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];
			
			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}
			
			itemStack.stackTagCompound.setTag("liquidTank", liquidStack.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public LiquidStack getLiquidStack(Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];
			
			if(itemStack.stackTagCompound == null) 
			{ 
				return null; 
			}
			
			if(itemStack.stackTagCompound.hasKey("liquidTank"))
			{
				return LiquidStack.loadLiquidStackFromNBT(itemStack.stackTagCompound.getCompoundTag("liquidTank"));
			}
		}
		
		return null;
	}

	@Override
	public boolean hasTank(Object... data) 
	{
		return data[0] instanceof ItemStack && ((ItemStack)data[0]).getItem() instanceof ISustainedTank && (((ItemStack)data[0]).getItemDamage() == 2);
	}
}
