package mekanism.common.item;

import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityPortableTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class ItemGaugeDropper extends ItemMekanism implements IGasItem, IFluidContainerItem
{
	public static int CAPACITY = FluidContainerRegistry.BUCKET_VOLUME;
	
	public static final int TRANSFER_RATE = 16;
	
	public ItemGaugeDropper()
	{
		super();
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	private void updateDamage(ItemStack stack)
	{
		GasStack gas = getGas_do(stack);
		FluidStack fluid = getFluid_do(stack);
		
		if((gas == null || gas.amount == 0) && (fluid == null || fluid.amount == 0))
		{
			stack.setItemDamage(100);
		}
		else if(gas != null && gas.amount > 0)
		{
			stack.setItemDamage((int)Math.max(1, (Math.abs((((float)gas.amount/getMaxGas(stack))*100)-100))));
		}
		else if(fluid != null && fluid.amount > 0)
		{
			stack.setItemDamage((int)Math.max(1, (Math.abs((((float)fluid.amount/getCapacity(stack))*100)-100))));
		}
	}
	
	public ItemStack getEmptyItem()
	{
		ItemStack empty = new ItemStack(this);
		empty.setItemDamage(100);
		setGas(empty, null);
		setFluid(empty, null);
		return empty;
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List list)
	{
		list.add(getEmptyItem());
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking() && !world.isRemote)
		{
			setGas(stack, null);
			setFluid(stack, null);
		
			return true;
		}
		
		return false;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		GasStack gasStack = getGas(itemstack);
		FluidStack fluidStack = getFluid(itemstack);

		if(gasStack == null && fluidStack == null)
		{
			list.add(MekanismUtils.localize("gui.empty") + ".");
		}
		else if(gasStack != null)
		{
			list.add(MekanismUtils.localize("tooltip.stored") + " " + gasStack.getGas().getLocalizedName() + ": " + gasStack.amount);
		}
		else if(fluidStack != null)
		{
			list.add(MekanismUtils.localize("tooltip.stored") + " " + fluidStack.getFluid().getLocalizedName(fluidStack) + ": " + fluidStack.amount);
		}
	}

	private FluidStack getFluid_do(ItemStack container) 
	{
		if(container.stackTagCompound == null)
		{
			return null;
		}

		if(container.stackTagCompound.hasKey("fluidStack"))
		{
			return FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("fluidStack"));
		}
		
		return null;
	}
	
	@Override
	public FluidStack getFluid(ItemStack container)
	{
		updateDamage(container);
		return getFluid_do(container);
	}
	
	public void setFluid(ItemStack container, FluidStack stack)
	{
		if(container.stackTagCompound == null)
		{
			container.setTagCompound(new NBTTagCompound());
		}
		
		if(stack == null || stack.amount == 0 || stack.fluidID == 0)
		{
			container.stackTagCompound.removeTag("fluidStack");
		}
		else {
			container.stackTagCompound.setTag("fluidStack", stack.writeToNBT(new NBTTagCompound()));
		}
		
		updateDamage(container);
	}

	@Override
	public int getCapacity(ItemStack container) 
	{
		return CAPACITY;
	}

	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill) 
	{
		FluidStack stored = getFluid(container);
		int toFill;

		if(stored != null && stored.getFluid() != resource.getFluid())
		{
			return 0;
		}
		
		if(stored == null)
		{
			toFill = Math.min(resource.amount, TileEntityPortableTank.MAX_FLUID);
		}
		else {
			toFill = Math.min(resource.amount, TileEntityPortableTank.MAX_FLUID-stored.amount);
		}
		
		if(doFill)
		{
			int fillAmount = toFill + (stored == null ? 0 : stored.amount);
			setFluid(container, new FluidStack(resource.getFluid(), (stored != null ? stored.amount : 0)+toFill));
		}
		
		return toFill;
	}

	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) 
	{
		FluidStack stored = getFluid(container);
		
		if(stored != null)
		{
			FluidStack toDrain = new FluidStack(stored.getFluid(), Math.min(stored.amount, maxDrain));
			
			if(doDrain)
			{
				stored.amount -= toDrain.amount;
				setFluid(container, stored.amount > 0 ? stored : null);
			}
			
			return toDrain;
		}
		
		return null;
	}

	@Override
	public int getRate(ItemStack itemstack) 
	{
		return TRANSFER_RATE;
	}

	@Override
	public int addGas(ItemStack itemstack, GasStack stack) 
	{
		if(getGas(itemstack) != null && getGas(itemstack).getGas() != stack.getGas())
		{
			return 0;
		}

		int toUse = Math.min(getMaxGas(itemstack)-getStored(itemstack), Math.min(getRate(itemstack), stack.amount));
		setGas(itemstack, new GasStack(stack.getGas(), getStored(itemstack)+toUse));

		return toUse;
	}

	@Override
	public GasStack removeGas(ItemStack itemstack, int amount) 
	{
		if(getGas(itemstack) == null)
		{
			return null;
		}

		Gas type = getGas(itemstack).getGas();

		int gasToUse = Math.min(getStored(itemstack), Math.min(getRate(itemstack), amount));
		setGas(itemstack, new GasStack(type, getStored(itemstack)-gasToUse));

		return new GasStack(type, gasToUse);
	}

	private int getStored(ItemStack itemstack)
	{
		return getGas(itemstack) != null ? getGas(itemstack).amount : 0;
	}
	
	@Override
	public boolean canReceiveGas(ItemStack itemstack, Gas type) 
	{
		return getGas(itemstack) == null || getGas(itemstack).getGas() == type;
	}

	@Override
	public boolean canProvideGas(ItemStack itemstack, Gas type)
	{
		return getGas(itemstack) != null && (type == null || getGas(itemstack).getGas() == type);
	}

	private GasStack getGas_do(ItemStack itemstack) 
	{
		if(itemstack.stackTagCompound == null)
		{
			return null;
		}

		return GasStack.readFromNBT(itemstack.stackTagCompound.getCompoundTag("gasStack"));
	}
	
	@Override
	public GasStack getGas(ItemStack itemstack)
	{
		updateDamage(itemstack);
		return getGas_do(itemstack);
	}

	@Override
	public void setGas(ItemStack itemstack, GasStack stack) 
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		if(stack == null || stack.amount == 0)
		{
			itemstack.stackTagCompound.removeTag("gasStack");
		}
		else {
			int amount = Math.max(0, Math.min(stack.amount, getMaxGas(itemstack)));
			GasStack gasStack = new GasStack(stack.getGas(), amount);

			itemstack.stackTagCompound.setTag("gasStack", gasStack.write(new NBTTagCompound()));
		}
		
		updateDamage(itemstack);
	}

	@Override
	public int getMaxGas(ItemStack itemstack) 
	{
		return CAPACITY;
	}

	@Override
	public boolean isMetadataSpecific(ItemStack itemstack) 
	{
		return false;
	}
}
