package mekanism.common;

import java.util.List;

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
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricInfo.ElectricUnit;
import universalelectricity.core.implement.IItemElectric;

public class ItemBlockEnergyCube extends ItemBlock implements IItemElectric, IEnergyCube
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
		
		list.add("Stored Energy: " + ElectricInfo.getDisplayShort(energy, ElectricUnit.JOULES));
	}
	
	@Override
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
    {
    	ItemBlockEnergyCube item = ((ItemBlockEnergyCube)itemstack.getItem());
    	item.setJoules(item.getJoules(itemstack), itemstack);
    	item.setTier(itemstack, item.getTier(itemstack));
    }
	
	public ItemStack getUnchargedItem(EnergyCubeTier tier)
	{
		ItemStack charged = new ItemStack(this);
		setTier(charged, tier);
		charged.setItemDamage(100);
		return charged;
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
			
			itemStack.setItemDamage((int)(getTier(itemStack).MAX_ELECTRICITY - electricityStored)/getTier(itemStack).DIVIDER);
			return electricityStored;
		}

		return -1;
	}

	@Override
	public void setJoules(double wattHours, Object... data)
	{
		if (data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack) data[0];

			if (itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			double electricityStored = Math.max(Math.min(wattHours, getMaxJoules(itemStack)), 0);
			itemStack.stackTagCompound.setDouble("electricity", electricityStored);
			itemStack.setItemDamage((int)(getTier(itemStack).MAX_ELECTRICITY - electricityStored)/getTier(itemStack).DIVIDER);
		}
	}

	@Override
	public double getMaxJoules(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
			return getTier(itemstack).MAX_ELECTRICITY;
		}
		return EnergyCubeTier.BASIC.MAX_ELECTRICITY;
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
		return true;
	}

	@Override
	public boolean canProduceElectricity()
	{
		return true;
	}
	
	@Override
	public String getItemNameIS(ItemStack itemstack)
	{
		return getItemName() + "." + getTier(itemstack).name;
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
    	boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    	
    	if (place)
    	{
    		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);
    		tileEntity.tier = ((IEnergyCube)stack.getItem()).getTier(stack);
    		tileEntity.electricityStored = getJoules(stack);
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
}
