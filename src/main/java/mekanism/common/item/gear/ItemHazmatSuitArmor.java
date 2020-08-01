package mekanism.common.item.gear;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemHazmatSuitArmor extends ArmorItem {

    private static final HazmatMaterial HAZMAT_MATERIAL = new HazmatMaterial();

    public ItemHazmatSuitArmor(EquipmentSlotType slot, Properties properties) {
        super(HAZMAT_MATERIAL, slot, properties.rarity(Rarity.UNCOMMON));
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
        if (stack.getTag() == null) {
            stack.setTag(new CompoundNBT());
        }
        stack.getTag().putInt("HideFlags", 2);
        return new ItemCapabilityWrapper(stack, RadiationShieldingHandler.create(item -> getShieldingByArmor(slot)));
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return material.getEnchantability() > 0;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class HazmatMaterial extends BaseSpecialArmorMaterial {

        @Override
        public String getName() {
            return Mekanism.MODID + ":hazmat";
        }
    }
}
