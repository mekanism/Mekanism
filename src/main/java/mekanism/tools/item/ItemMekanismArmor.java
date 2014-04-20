package mekanism.tools.item;

import java.util.List;

import mekanism.client.render.ModelCustomArmor;
import mekanism.common.Mekanism;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMekanismArmor extends ItemArmor
{
	public ItemMekanismArmor(ArmorMaterial enumarmormaterial, int renderIndex, int armorType)
	{
		super(enumarmormaterial, renderIndex, armorType);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add("HP: " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		return "mekanism:armor/" + getArmorMaterial().name().toLowerCase() + "_" + type + ".png";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot)
	{
		if(itemStack.getItem() == MekanismTools.GlowstoneHelmet || itemStack.getItem() == MekanismTools.GlowstoneChestplate ||
				itemStack.getItem() == MekanismTools.GlowstoneLeggings || itemStack.getItem() == MekanismTools.GlowstoneBoots)
		{
			return ModelCustomArmor.getGlow(armorSlot);
		}

		return super.getArmorModel(entityLiving, itemStack, armorSlot);
	}
}
