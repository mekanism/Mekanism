package mekanism.common.item;

import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.common.Mekanism;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGasMask extends ItemArmor
{
	public ItemGasMask()
	{
		super(EnumHelper.addArmorMaterial("GASMASK", 0, new int[] {0, 0, 0, 0}, 0), 0, 0);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity)
	{
		return armorType == 0;
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
		model.modelType = ArmorModel.GASMASK;
		return model;
	}

	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event)
	{
		EntityLivingBase base = event.entityLiving;

		if(base.getEquipmentInSlot(4) != null && base.getEquipmentInSlot(4).getItem() instanceof ItemGasMask)
		{
			ItemGasMask mask = (ItemGasMask)base.getEquipmentInSlot(4).getItem();

			if(base.getEquipmentInSlot(3) != null && base.getEquipmentInSlot(3).getItem() instanceof ItemScubaTank)
			{
				ItemScubaTank tank = (ItemScubaTank)base.getEquipmentInSlot(3).getItem();

				if(tank.getFlowing(base.getEquipmentInSlot(3)) && tank.getGas(base.getEquipmentInSlot(3)) != null)
				{
					if(event.source == DamageSource.magic)
					{
						event.setCanceled(true);
					}
				}
			}
		}
	}
}
