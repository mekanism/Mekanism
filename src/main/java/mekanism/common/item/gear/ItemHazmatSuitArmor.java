package mekanism.common.item.gear;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemHazmatSuitArmor extends ArmorItem {

    private static final HazmatMaterial HAZMAT_MATERIAL = new HazmatMaterial();

    public ItemHazmatSuitArmor(EquipmentSlotType slot, Properties properties) {
        super(HAZMAT_MATERIAL, slot, properties);
    }

    public static double getShieldingByArmor(EquipmentSlotType type) {
        if (type == EquipmentSlotType.HEAD) {
            return 0.25;
        } else if (type == EquipmentSlotType.CHEST) {
            return 0.4;
        } else if (type == EquipmentSlotType.LEGS) {
            return 0.2;
        } else if (type == EquipmentSlotType.FEET) {
            return 0.15;
        }
        return 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RadiationShieldingHandler.create(item -> getShieldingByArmor(slot)));
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class HazmatMaterial implements IArmorMaterial {

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
            return "hazmat";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }
}
