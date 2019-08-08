package mekanism.common.item.gear;

import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemGasMask extends ItemArmorMekanism {

    public ItemGasMask() {
        super(EnumHelper.addArmorMaterial("GASMASK", "gasmask", 0, new int[]{0, 0, 0, 0}, 0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
              0), EquipmentSlotType.HEAD, "gas_mask");
    }

    @Override
    public boolean isValidArmor(ItemStack stack, EquipmentSlotType armorType, Entity entity) {
        return armorType == EquipmentSlotType.HEAD;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/NullArmor.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ArmorModel.GASMASK;
        return model;
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event) {
        LivingEntity base = event.getEntityLiving();
        ItemStack headStack = base.getItemStackFromSlot(EquipmentSlotType.HEAD);
        ItemStack chestStack = base.getItemStackFromSlot(EquipmentSlotType.CHEST);
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