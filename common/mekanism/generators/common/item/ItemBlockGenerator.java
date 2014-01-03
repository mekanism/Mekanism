package mekanism.generators.common.item;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.ISustainedInventory;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.integration.IC2ItemManager;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.common.util.MekanismUtils;
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

import java.util.List;

/**
 * Item class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
 * 5: Advanced Solar Generator
 * 6: Wind Turbine
 * @author AidanBrady
 *
 */
public class ItemBlockGenerator extends ItemBlock implements IEnergizedItem, ISpecialElectricItem, ISustainedInventory, ISustainedTank, IEnergyContainerItem
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
		return getUnlocalizedName() + "." + GeneratorType.getFromMetadata(itemstack.getItemDamage()).name;
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
			list.add(EnumColor.BRIGHT_GREEN + "Stored Energy: " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
			
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
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		boolean place = true;
		int blockID = world.getBlockId(x, y, z);
		
		if(stack.getItemDamage() == GeneratorType.ADVANCED_SOLAR_GENERATOR.meta)
		{
			if(blockID != 0 && !Block.blocksList[blockID].isBlockReplaceable(world, x, y, z))
				return false;
	        
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
			if(blockID != 0 && !Block.blocksList[blockID].isBlockReplaceable(world, x, y, z))
				return false;
	        
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
		return 4;
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
		return false;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return true;
	}
	
	@Override
	public int receiveEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canReceive(theItem))
		{
			double energyNeeded = getMaxEnergy(theItem)-getEnergy(theItem);
			double toReceive = Math.min(energy*Mekanism.FROM_TE, energyNeeded);
			
			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) + toReceive);
			}
			
			return (int)Math.round(toReceive*Mekanism.TO_TE);
		}
		
		return 0;
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean simulate) 
	{
		if(canSend(theItem))
		{
			double energyRemaining = getEnergy(theItem);
			double toSend = Math.min((energy*Mekanism.FROM_TE), energyRemaining);
			
			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) - toSend);
			}
			
			return (int)Math.round(toSend*Mekanism.TO_TE);
		}
		
		return 0;
	}

	@Override
	public int getEnergyStored(ItemStack theItem)
	{
		return (int)(getEnergy(theItem)*Mekanism.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int)(getMaxEnergy(theItem)*Mekanism.TO_TE);
	}
	
	@Override
	public boolean isMetadataSpecific()
	{
		return true;
	}
	
	@Override
	public IElectricItemManager getManager(ItemStack itemStack) 
	{
		return IC2ItemManager.getManager(this);
	}
}
