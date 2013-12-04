package mekanism.common.item;

import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.common.Mekanism;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemJetpack extends ItemMekanism
{
	public ItemJetpack(int id)
	{
		super(id);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity)
    {
    	return armorType == 1;
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
