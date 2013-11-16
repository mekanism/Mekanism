/**
 * 
 */
package mekanism.induction.common.item;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.induction.common.MekanismInduction;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import universalelectricity.compatibility.ItemUniversalElectric;

/**
 * Stores power.
 * 
 * @author Calclavia
 * 
 */
public class ItemInfiniteCapacitor extends ItemUniversalElectric
{
	public ItemInfiniteCapacitor(int id)
	{
		super(id);
		this.setCreativeTab(Mekanism.tabMekanism);
		this.setTextureName("mekanism:Capacitor");
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
	}
	
	@Override
	public void registerIcons(IconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:Capacitor");
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
	{
		list.add("Infinite");
	}

	@Override
	public float recharge(ItemStack itemStack, float energy, boolean doReceive)
	{
		return energy;
	}

	@Override
	public float discharge(ItemStack itemStack, float energy, boolean doTransfer)
	{
		return energy;
	}

	@Override
	public void setElectricity(ItemStack itemStack, float joules)
	{

	}

	@Override
	public float getTransfer(ItemStack itemStack)
	{
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public float getElectricityStored(ItemStack itemStack)
	{
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public float getMaxElectricityStored(ItemStack theItem)
	{
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(this));
	}
}
