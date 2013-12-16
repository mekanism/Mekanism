package mekanism.tools.item;

import java.util.List;

import mekanism.client.render.ModelCustomArmor;
import mekanism.common.Mekanism;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMekanismArmor extends ItemArmor
{
    public ItemMekanismArmor(int id, EnumArmorMaterial enumarmormaterial, int renderIndex, int armorType)
    {
        super(id, enumarmormaterial, renderIndex, armorType);
        setCreativeTab(Mekanism.tabMekanism);
    }
    
    @Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add("HP: " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}
    
	@Override
	public void registerIcons(IconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
	
	@Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer)
    {
		return "mekanism:armor/" + getArmorMaterial().name().toLowerCase() + "_" + layer + ".png";
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot)
    {
		if(itemStack.itemID == MekanismTools.GlowstoneHelmet.itemID || itemStack.itemID == MekanismTools.GlowstoneChestplate.itemID ||
				itemStack.itemID == MekanismTools.GlowstoneLeggings.itemID || itemStack.itemID == MekanismTools.GlowstoneBoots.itemID)
		{
			return ModelCustomArmor.getGlow(armorSlot);
		}
		
		return super.getArmorModel(entityLiving, itemStack, armorSlot);
    }
}
