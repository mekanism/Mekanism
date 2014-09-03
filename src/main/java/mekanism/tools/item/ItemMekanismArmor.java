package mekanism.tools.item;

import java.util.List;

import mekanism.api.util.StackUtils;
import mekanism.client.render.ModelCustomArmor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.util.MekanismUtils;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsItems;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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
		list.add(MekanismUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		int layer = (slot == 2) ? 2 : 1;
		return "mekanism:armor/" + getArmorMaterial().name().toLowerCase() + "_" + layer + ".png";
	}
	
	@Override
    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
    {
        return StackUtils.equalsWildcard(getRepairStack(), stack2) ? true : super.getIsRepairable(stack1, stack2);
    }
	
    private ItemStack getRepairStack()
    {
    	if(getArmorMaterial() == MekanismTools.armorOBSIDIAN)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 0);
    	}
    	else if(getArmorMaterial() == MekanismTools.armorLAZULI)
    	{
    		return new ItemStack(Items.dye, 1, 4);
    	}
    	else if(getArmorMaterial() == MekanismTools.armorOSMIUM)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 1);
    	}
    	else if(getArmorMaterial() == MekanismTools.armorBRONZE)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 2);
    	}
    	else if(getArmorMaterial() == MekanismTools.armorGLOWSTONE)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 3);
    	}
    	else if(getArmorMaterial() == MekanismTools.armorSTEEL)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 4);
    	}
    	
    	return new ItemStack(getArmorMaterial().func_151685_b());
    }

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot)
	{
		if(itemStack.getItem() == ToolsItems.GlowstoneHelmet || itemStack.getItem() == ToolsItems.GlowstoneChestplate ||
				itemStack.getItem() == ToolsItems.GlowstoneLeggings || itemStack.getItem() == ToolsItems.GlowstoneBoots)
		{
			return ModelCustomArmor.getGlow(armorSlot);
		}

		return super.getArmorModel(entityLiving, itemStack, armorSlot);
	}
}
