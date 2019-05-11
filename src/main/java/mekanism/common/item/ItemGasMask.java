package mekanism.common.item;

import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.common.Mekanism;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemGasMask extends ItemArmor {

    public ItemGasMask() {
        super(EnumHelper.addArmorMaterial("GASMASK", "gasmask", 0, new int[]{0, 0, 0, 0}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
              0), 0, EntityEquipmentSlot.HEAD);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
        return armorType == EntityEquipmentSlot.HEAD;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "mekanism:render/NullArmor.png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.GASMASK;
        return model;
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        EntityLivingBase base = event.getEntityLiving();
        ItemStack headStack = base.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        ItemStack chestStack = base.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!headStack.isEmpty() && headStack.getItem() instanceof ItemGasMask) {
            ItemGasMask mask = (ItemGasMask) headStack.getItem();
            if (!chestStack.isEmpty() && chestStack.getItem() instanceof ItemScubaTank) {
                ItemScubaTank tank = (ItemScubaTank) chestStack.getItem();
                if (tank.getFlowing(chestStack) && tank.getGas(chestStack) != null) {
                    if (event.getSource() == DamageSource.MAGIC) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}