package mekanism.common.item.gear;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.ModelCustomArmor.ArmorModel;
import mekanism.client.render.item.gear.RenderGasMask;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemGasMask extends ItemCustomArmorMekanism {

    public static final GasMaskMaterial GAS_MASK_MATERIAL = new GasMaskMaterial();

    public ItemGasMask() {
        super(GAS_MASK_MATERIAL, EquipmentSlotType.HEAD, "gas_mask", new Item.Properties().setTEISR(() -> RenderGasMask::new));
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

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class GasMaskMaterial implements IArmorMaterial {

        @Override
        public int getDurability(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        }

        @Override
        public Ingredient getRepairMaterial() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "gas_mask";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }
}