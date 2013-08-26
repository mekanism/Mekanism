package mekanism.generators.common;

import ic2.api.item.ICustomElectricItem;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.ISustainedInventory;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.generators.common.block.BlockGenerator.GeneratorType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.input.Keyboard;

import thermalexpansion.api.item.IChargeableItem;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import universalelectricity.core.item.IItemElectric;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 2: Electrolytic Separator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
 * 5: Advanced Solar Generator
 * 6: Wind Turbine
 * @author AidanBrady
 *
 */
public class ItemBlockGenerator extends ItemBlock implements IEnergizedItem, IItemElectric, ICustomElectricItem, ISustainedInventory, ISustainedTank, IChargeableItem
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
			case 6:
				name = "WindTurbine";
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
			list.add(EnumColor.BRIGHT_GREEN + "Stored Energy: " + EnumColor.GREY + ElectricityDisplay.getDisplayShort(getElectricityStored(itemstack), ElectricUnit.JOULES));
			list.add(EnumColor.BRIGHT_GREEN + "Voltage: " + EnumColor.GREY + getVoltage(itemstack) + "v");
			
			if(hasTank(itemstack))
			{
				if(getFluidStack(itemstack) != null)
				{
					list.add(EnumColor.PINK + FluidRegistry.getFluidName(getFluidStack(itemstack)) + ": " + EnumColor.GREY + getFluidStack(itemstack).amount + "mB");
				}
			}
			
			list.add(EnumColor.AQUA + "Inventory: " + EnumColor.GREY + (getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
		}
	}

	@Override
	public float getVoltage(ItemStack itemStack) 
	{
		return itemStack.getItemDamage() == 3 ? 240 : 120;
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
					if(world.getBlockId(x+xPos, y+2, z+zPos) != 0 || y+2 > 255) 
						place = false;
				}
			}
		}
		else if(stack.getItemDamage() == GeneratorType.WIND_TURBINE.meta)
		{
	        if(world.getBlockId(x, y, z) != Block.tallGrass.blockID && world.getBlockId(x, y, z) != 0) 
	        	place = false;
	        
	        if(world.getBlockId(x, y, z) != 0)
	        {
	        	if(Block.blocksList[world.getBlockId(x, y, z)].isBlockReplaceable(world, x, y, z)) 
	        		place = true; 
	        }
	        
			for(int yPos = y+1; yPos <= y+4; yPos++)
			{
				if(world.getBlockId(x, yPos, z) != 0 || yPos > 255) 
					place = false;
			}
		}
		
		if(place && super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
		{
    		TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    		tileEntity.electricityStored = getEnergy(stack);
    		
    		((ISustainedInventory)tileEntity).setInventory(getInventory(stack));
    		
    		if(tileEntity instanceof ISustainedTank)
    		{
    			if(hasTank(stack) && getFluidStack(stack) != null)
    			{
    				((ISustainedTank)tileEntity).setFluidStack(getFluidStack(stack), stack);
    			}
    		}
    		
    		return true;
		}
		
		return false;
    }
	
	@Override
	public int charge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		if(canReceive(itemStack))
		{
			double energyNeeded = getMaxEnergy(itemStack)-getEnergy(itemStack);
			double energyToStore = Math.min(Math.min(amount*Mekanism.FROM_IC2, getMaxEnergy(itemStack)*0.01), energyNeeded);
			
			if(!simulate)
			{
				setEnergy(itemStack, getEnergy(itemStack) + energyToStore);
			}
			
			return (int)(energyToStore*Mekanism.TO_IC2);
		}
		
		return 0;
	}
	
	@Override
	public int discharge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		if(canSend(itemStack))
		{
			double energyWanted = amount*Mekanism.FROM_IC2;
			double energyToGive = Math.min(Math.min(energyWanted, getMaxEnergy(itemStack)*0.01), getEnergy(itemStack));
			
			if(!simulate)
			{
				setEnergy(itemStack, getEnergy(itemStack) - energyToGive);
			}
			
			return (int)(energyToGive*Mekanism.TO_IC2);
		}
		
		return 0;
	}

	@Override
	public boolean canUse(ItemStack itemStack, int amount)
	{
		return getEnergy(itemStack) >= amount*Mekanism.FROM_IC2;
	}
	
	@Override
	public boolean canShowChargeToolTip(ItemStack itemStack)
	{
		return false;
	}
	
	@Override
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return canSend(itemStack);
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
	public void setFluidStack(FluidStack fluidStack, Object... data) 
	{
		if(fluidStack == null || fluidStack.amount == 0 || fluidStack.fluidID == 0)
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
			
			itemStack.stackTagCompound.setTag("fluidTank", fluidStack.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public FluidStack getFluidStack(Object... data) 
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];
			
			if(itemStack.stackTagCompound == null) 
			{ 
				return null; 
			}
			
			if(itemStack.stackTagCompound.hasKey("fluidTank"))
			{
				return FluidStack.loadFluidStackFromNBT(itemStack.stackTagCompound.getCompoundTag("fluidTank"));
			}
		}
		
		return null;
	}

	@Override
	public boolean hasTank(Object... data) 
	{
		return data[0] instanceof ItemStack && ((ItemStack)data[0]).getItem() instanceof ISustainedTank && (((ItemStack)data[0]).getItemDamage() == 2);
	}
	
	@Override
	public double getEnergy(ItemStack itemStack) 
	{
		if(itemStack.stackTagCompound == null) 
		{ 
			return 0; 
		}
		
		return itemStack.stackTagCompound.getDouble("electricity");
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount) 
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0);
		itemStack.stackTagCompound.setDouble("electricity", electricityStored);
	}

	@Override
	public double getMaxEnergy(ItemStack itemStack) 
	{
		return GeneratorType.getFromMetadata(itemStack.getItemDamage()).maxEnergy;
	}

	@Override
	public double getMaxTransfer(ItemStack itemStack) 
	{
		return getMaxEnergy(itemStack)*0.005;
	}

	@Override
	public boolean canReceive(ItemStack itemStack) 
	{
		return itemStack.getItemDamage() == GeneratorType.ELECTROLYTIC_SEPARATOR.meta;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return itemStack.getItemDamage() != GeneratorType.ELECTROLYTIC_SEPARATOR.meta;
	}
	
	@Override
	public float receiveEnergy(ItemStack theItem, float energy, boolean doReceive)
	{
		return (float)(recharge(theItem, (float)((energy*Mekanism.FROM_BC)*Mekanism.TO_UE), doReceive)*Mekanism.TO_BC);
	}

	@Override
	public float transferEnergy(ItemStack theItem, float energy, boolean doTransfer) 
	{
		return (float)(discharge(theItem, (float)((energy*Mekanism.FROM_BC)*Mekanism.TO_UE), doTransfer)*Mekanism.TO_BC);
	}

	@Override
	public float getEnergyStored(ItemStack theItem)
	{
		return (float)(getEnergy(theItem)*Mekanism.TO_BC);
	}

	@Override
	public float getMaxEnergyStored(ItemStack theItem)
	{
		return (float)(getMaxEnergy(theItem)*Mekanism.TO_BC);
	}
	
	@Override
	public boolean isMetadataSpecific()
	{
		return true;
	}
	
	@Override
	public float recharge(ItemStack itemStack, float energy, boolean doRecharge) 
	{
		if(canReceive(itemStack))
		{
			double energyNeeded = getMaxEnergy(itemStack)-getEnergy(itemStack);
			double toReceive = Math.min(energy*Mekanism.FROM_UE, energyNeeded);
			
			if(doRecharge)
			{
				setEnergy(itemStack, getEnergy(itemStack) + toReceive);
			}
			
			return (float)(toReceive*Mekanism.TO_UE);
		}
		
		return 0;
	}

	@Override
	public float discharge(ItemStack itemStack, float energy, boolean doDischarge) 
	{
		if(canSend(itemStack))
		{
			double energyRemaining = getEnergy(itemStack);
			double toSend = Math.min((energy*Mekanism.FROM_UE), energyRemaining);
			
			if(doDischarge)
			{
				setEnergy(itemStack, getEnergy(itemStack) - toSend);
			}
			
			return (float)(toSend*Mekanism.TO_UE);
		}
		
		return 0;
	}

	@Override
	public float getElectricityStored(ItemStack theItem) 
	{
		return (float)(getEnergy(theItem)*Mekanism.TO_UE);
	}

	@Override
	public float getMaxElectricityStored(ItemStack theItem) 
	{
		return (float)(getMaxEnergy(theItem)*Mekanism.TO_UE);
	}

	@Override
	public void setElectricity(ItemStack itemStack, float joules) 
	{
		setEnergy(itemStack, joules*Mekanism.TO_UE);
	}

	@Override
	public float getTransfer(ItemStack itemStack)
	{
		return (float)(getMaxTransfer(itemStack)*Mekanism.TO_UE);
	}
}
