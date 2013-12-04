package mekanism.common.item;

import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.EnumHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemJetpack extends ItemArmor
{
	public ItemJetpack(int id)
	{
		super(id, EnumHelper.addArmorMaterial("JETPACK", 0, new int[] {0, 0, 0, 0}, 0), 0, 1);
		//setCreativeTab(Mekanism.tabMekanism);
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
		model.modelType = ArmorModel.JETPACK;
        return model;
    }
}
