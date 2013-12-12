package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemJetpack.JetpackMode;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.EnumHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemScubaTank extends ItemArmor implements IGasItem
{
	public int MAX_GAS = 24000;
	public int TRANSFER_RATE = 16;
	
	public ItemScubaTank(int id)
	{
		super(id, EnumHelper.addArmorMaterial("SCUBATANK", 0, new int[] {0, 0, 0, 0}, 0), 0, 1);
		setCreativeTab(Mekanism.tabMekanism);
		setMaxDamage(100);
		setNoRepair();
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		GasStack gasStack = getGas(itemstack);
		
		if(gasStack == null)
		{
			list.add("No gas stored.");
		}
		else {
			list.add("Stored " + gasStack.getGas().getLocalizedName() + ": " + gasStack.amount);
		}
		
		list.add(EnumColor.GREY + "Flowing: " + (getFlowing(itemstack) ? EnumColor.DARK_GREEN : EnumColor.DARK_RED) + getFlowing(itemstack));
	}
	
	@Override
    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity)
    {
    	return armorType == 1;
    }
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
    {
		return "mekanism:render/NullArmor.png";
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot)
    {
		ModelCustomArmor model = ModelCustomArmor.INSTANCE;
		model.modelType = ArmorModel.SCUBATANK;
        return model;
    }
	
	public void useGas(ItemStack stack)
	{
		setGas(new GasStack(getGas(stack).getGas(), getGas(stack).amount-1), stack);
	}
	
	@Override
	public int getMaxGas(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			return MAX_GAS;
		}
		
		return 0;
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
		
		if(stack.getGas() != GasRegistry.getGas("oxygen"))
		{
			return 0;
		}
		
		int toUse = Math.min(getMaxGas(itemstack)-getStored(itemstack), Math.min(getRate(itemstack), stack.amount));
		setGas(new GasStack(stack.getGas(), getStored(itemstack)+toUse), itemstack);
		
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
		setGas(new GasStack(type, getStored(itemstack)-gasToUse), itemstack);
		
		return new GasStack(type, gasToUse);
	}
	
	public int getStored(ItemStack itemstack)
	{
		return getGas(itemstack) != null ? getGas(itemstack).amount : 0;
	}
	
	public void toggleFlowing(ItemStack stack)
	{
		setFlowing(stack, !getFlowing(stack));
	}
	
	public boolean getFlowing(ItemStack stack)
	{
		if(stack.stackTagCompound == null)
		{
			return false;
		}
		
		return stack.stackTagCompound.getBoolean("flowing");
	}
	
	public void setFlowing(ItemStack stack, boolean flowing)
	{
		if(stack.stackTagCompound == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		stack.stackTagCompound.setBoolean("flowing", flowing);
	}
	
	@Override
	public boolean canReceiveGas(ItemStack itemstack, Gas type)
	{
		return type == GasRegistry.getGas("oxygen");
	}
	
	@Override
	public boolean canProvideGas(ItemStack itemstack, Gas type)
	{
		return getGas(itemstack) != null && (type == null || getGas(itemstack).getGas() == type);
	}
	
	@Override
	public GasStack getGas(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
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
		
		return null;
	}
	
	@Override
	public void setGas(GasStack stack, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
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
				itemstack.stackTagCompound.setCompoundTag("stored", gasStack.write(new NBTTagCompound()));
			}
		}
	}
	
	public ItemStack getEmptyItem()
	{
		ItemStack empty = new ItemStack(this);
		setGas(null, empty);
		empty.setItemDamage(100);
		return empty;
	}
	
	@Override
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack empty = new ItemStack(this);
		setGas(null, empty);
		empty.setItemDamage(100);
		list.add(empty);
		
		ItemStack filled = new ItemStack(this);
		setGas(new GasStack(GasRegistry.getGas("oxygen"), ((IGasItem)filled.getItem()).getMaxGas(filled)), filled);
		list.add(filled);
	}
}
