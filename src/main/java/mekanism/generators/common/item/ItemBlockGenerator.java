package mekanism.generators.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.integration.IC2ItemManager;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.block.BlockGenerator.GeneratorType;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cofh.api.energy.IEnergyContainerItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

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

@InterfaceList({
		@Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI|energy"),
		@Interface(iface = "ic2.api.item.ISpecialElectricItem", modid = "IC2API")
})
public class ItemBlockGenerator extends ItemBlock implements IEnergizedItem, ISpecialElectricItem, ISustainedInventory, ISustainedTank, IEnergyContainerItem
{
	public Block metaBlock;

	public ItemBlockGenerator(Block block)
	{
		super(block);
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
	public String getUnlocalizedName(ItemStack itemstack)
	{
		if(GeneratorType.getFromMetadata(itemstack.getItemDamage()) == null)
		{
			return "KillMe!";
		}

		return getUnlocalizedName() + "." + GeneratorType.getFromMetadata(itemstack.getItemDamage()).name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		GeneratorType type = GeneratorType.getFromMetadata(itemstack.getItemDamage());
		
		if(!MekKeyHandler.isPressed(MekanismKeyHandler.sneakKey))
		{
			list.add(MekanismUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.forDetails") + ".");
			list.add(MekanismUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " and " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.modeSwitchKey.getKeyCode()) + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.forDesc") + ".");
		}
		else if(!MekKeyHandler.isPressed(MekanismKeyHandler.modeSwitchKey))
		{
			list.add(EnumColor.BRIGHT_GREEN + MekanismUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));

			if(hasTank(itemstack))
			{
				if(getFluidStack(itemstack) != null)
				{
					list.add(EnumColor.PINK + FluidRegistry.getFluidName(getFluidStack(itemstack)) + ": " + EnumColor.GREY + getFluidStack(itemstack).amount + "mB");
				}
			}

			list.add(EnumColor.AQUA + MekanismUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
		}
		else {
			list.addAll(MekanismUtils.splitLines(type.getDescription()));
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		boolean place = true;
		Block block = world.getBlockState(pos).getBlock();

		if(stack.getItemDamage() == GeneratorType.ADVANCED_SOLAR_GENERATOR.meta)
		{
			if(!(block.isReplaceable(world, pos) && world.isAirBlock(pos.add(0,1,0))))
			{
				return false;
			}

			BlockPos currentPos = null;

			outer:
			for(int xPos = -1; xPos <= 1; xPos++)
			{
				for(int zPos =- 1; zPos <= 1; zPos++)
				{
					currentPos = pos.add(xPos, 2, zPos);
					if(!world.isAirBlock(currentPos) || pos.getY()+2 > 255)
					{
						place = false;
						break outer;
					}
				}
			}
		}
		else if(stack.getItemDamage() == GeneratorType.WIND_TURBINE.meta)
		{
			if(!block.isReplaceable(world, pos))
			{
				return false;
			}

			BlockPos currentPos = null;

			outer:
			for(int yPos = 1; yPos <= 4; yPos++)
			{
				currentPos = pos.add(0, yPos, 0);
				if(!world.isAirBlock(currentPos) || pos.getY() + yPos > 255)
				{
					place = false;
					break outer;
				}
			}
		}

		if(place && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState))
		{
			TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getTileEntity(pos);
			tileEntity.electricityStored = getEnergy(stack);

			tileEntity.setInventory(getInventory(stack));
			
			if(tileEntity instanceof ISustainedData)
			{
				if(stack.getTagCompound() != null)
				{
					((ISustainedData)tileEntity).readSustainedData(stack);
				}
			}

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
	@Method(modid = "IC2API")
	public boolean canProvideEnergy(ItemStack itemStack)
	{
		return canSend(itemStack);
	}

	@Override
	@Method(modid = "IC2API")
	public Item getChargedItem(ItemStack itemStack)
	{
		return this;
	}

	@Override
	@Method(modid = "IC2API")
	public Item getEmptyItem(ItemStack itemStack)
	{
		return this;
	}

	@Override
	@Method(modid = "IC2API")
	public double getMaxCharge(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	@Method(modid = "IC2API")
	public int getTier(ItemStack itemStack)
	{
		return 4;
	}

	@Override
	@Method(modid = "IC2API")
	public double getTransferLimit(ItemStack itemStack)
	{
		return 0;
	}

	@Override
	public void setInventory(NBTTagList nbtTags, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.getTagCompound().setTag("Items", nbtTags);
		}
	}

	@Override
	public NBTTagList getInventory(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.getTagCompound() == null)
			{
				return null;
			}

			return itemStack.getTagCompound().getTagList("Items", 10);
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

			if(itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.getTagCompound().setTag("fluidTank", fluidStack.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public FluidStack getFluidStack(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.getTagCompound() == null)
			{
				return null;
			}

			if(itemStack.getTagCompound().hasKey("fluidTank"))
			{
				return FluidStack.loadFluidStackFromNBT(itemStack.getTagCompound().getCompoundTag("fluidTank"));
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
		if(itemStack.getTagCompound() == null)
		{
			return 0;
		}

		return itemStack.getTagCompound().getDouble("electricity");
	}

	@Override
	public void setEnergy(ItemStack itemStack, double amount)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0);
		itemStack.getTagCompound().setDouble("electricity", electricityStored);
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
			double toReceive = Math.min(energy* general.FROM_TE, energyNeeded);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) + toReceive);
			}

			return (int)Math.round(toReceive* general.TO_TE);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ItemStack theItem, int energy, boolean simulate)
	{
		if(canSend(theItem))
		{
			double energyRemaining = getEnergy(theItem);
			double toSend = Math.min((energy* general.FROM_TE), energyRemaining);

			if(!simulate)
			{
				setEnergy(theItem, getEnergy(theItem) - toSend);
			}

			return (int)Math.round(toSend* general.TO_TE);
		}

		return 0;
	}

	@Override
	public int getEnergyStored(ItemStack theItem)
	{
		return (int)(getEnergy(theItem)* general.TO_TE);
	}

	@Override
	public int getMaxEnergyStored(ItemStack theItem)
	{
		return (int)(getMaxEnergy(theItem)* general.TO_TE);
	}

	@Override
	public boolean isMetadataSpecific(ItemStack itemStack)
	{
		return true;
	}

	@Override
	@Method(modid = "IC2API")
	public IElectricItemManager getManager(ItemStack itemStack)
	{
		return IC2ItemManager.getManager(this);
	}
}
