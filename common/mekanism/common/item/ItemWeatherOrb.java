package mekanism.common.item;

import mekanism.common.Mekanism;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemWeatherOrb extends ItemMekanism
{
	public ItemWeatherOrb(int i)
	{
		super(i);
		setMaxStackSize(1);
		setMaxDamage(5000);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public boolean hasEffect(ItemStack itemstack)
	{
		return true;
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(itemstack.getItemDamage() == 0)
		{
			entityplayer.openGui(Mekanism.instance, 2, world, 0, 0, 0);
		}
		return itemstack;
	}
	
	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
	{
		if(itemstack.getItemDamage() > 0)
		{
			itemstack.damageItem(-1, (EntityLivingBase)entity);
		}
	}
}
