package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class ItemWeatherOrb extends ItemObsidian
{
	public ItemWeatherOrb(int i)
	{
		super(i);
		setMaxStackSize(1);
		setMaxDamage(5000);
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	public boolean hasEffect(ItemStack itemstack)
	{
		return true;
	}
	
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(itemstack.getItemDamage() == 0)
		{
			entityplayer.openGui(ObsidianIngots.instance, 2, world, (int)entityplayer.posX, (int)entityplayer.posY, (int)entityplayer.posZ);
		}
		return itemstack;
	}
	
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
	{
		if(itemstack.getItemDamage() > 0)
		{
			itemstack.damageItem(-1, (EntityLiving)entity);
		}
	}
}
