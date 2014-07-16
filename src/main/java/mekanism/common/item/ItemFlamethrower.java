package mekanism.common.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.item.ItemJetpack.JetpackMode;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemFlamethrower extends ItemMekanism implements IGasItem
{
	public int MAX_GAS = 24000;
	public int TRANSFER_RATE = 16;
	
	public ItemFlamethrower()
	{
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {}
	
	public void useGas(ItemStack stack)
	{
		setGas(stack, new GasStack(getGas(stack).getGas(), getGas(stack).amount-1));
	}

	@Override
	public int getMaxGas(ItemStack itemstack)
	{
		return MAX_GAS;
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

		if(stack.getGas() != GasRegistry.getGas("hydrogen"))
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

	public int getStored(ItemStack itemstack)
	{
		return getGas(itemstack) != null ? getGas(itemstack).amount : 0;
	}

	@Override
	public boolean canReceiveGas(ItemStack itemstack, Gas type)
	{
		return type == GasRegistry.getGas("hydrogen");
	}

	@Override
	public boolean canProvideGas(ItemStack itemstack, Gas type)
	{
		return false;
	}

	@Override
	public GasStack getGas(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return null;
		}

		GasStack stored = GasStack.readFromNBT(itemstack.stackTagCompound.getCompoundTag("stored"));

		if(stored == null)
		{
			itemstack.setItemDamage(100);
		}
		else {
			itemstack.setItemDamage((int)Math.max(1, (Math.abs((((float)stored.amount/getMaxGas(itemstack))*100)-100))));
		}

		return stored;
	}
	
	@Override
	public boolean isMetadataSpecific(ItemStack itemStack)
	{
		return false;
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
			itemstack.setItemDamage(100);
			itemstack.stackTagCompound.removeTag("stored");
		}
		else {
			int amount = Math.max(0, Math.min(stack.amount, getMaxGas(itemstack)));
			GasStack gasStack = new GasStack(stack.getGas(), amount);

			itemstack.setItemDamage((int)Math.max(1, (Math.abs((((float)amount/getMaxGas(itemstack))*100)-100))));
			itemstack.stackTagCompound.setTag("stored", gasStack.write(new NBTTagCompound()));
		}
	}
	
	public boolean getInUse(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return false;
		}
		
		return itemstack.stackTagCompound.getBoolean("inUse");
	}
	
	public void setInUse(ItemStack itemstack, boolean state)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}
		
		itemstack.stackTagCompound.setBoolean("inUse", state);
	}

	public ItemStack getEmptyItem()
	{
		ItemStack empty = new ItemStack(this);
		setGas(empty, null);
		empty.setItemDamage(100);
		return empty;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List list)
	{
		ItemStack empty = new ItemStack(this);
		setGas(empty, null);
		empty.setItemDamage(100);
		list.add(empty);

		ItemStack filled = new ItemStack(this);
		setGas(filled, new GasStack(GasRegistry.getGas("hydrogen"), ((IGasItem)filled.getItem()).getMaxGas(filled)));
		list.add(filled);
	}
}
