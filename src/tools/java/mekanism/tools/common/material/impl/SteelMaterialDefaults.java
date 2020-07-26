package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class SteelMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 1_792;
    }

    @Override
    public float getAxeDamage() {
        return 4;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.0F;
    }

    @Override
    public float getPaxelDamage() {
        return 8;
    }

    @Override
    public int getPaxelHarvestLevel() {
        return 3;
    }

    @Override
    public int getPaxelMaxUses() {
        return 1_250;
    }

    @Override
    public float getPaxelEfficiency() {
        return 18;
    }

    @Override
    public int getMaxUses() {
        return 850;
    }

    @Override
    public float getEfficiency() {
        return 14;
    }

    @Override
    public float getAttackDamage() {
        return 4;
    }

    @Override
    public int getHarvestLevel() {
        return 3;
    }

    @Override
    public int getCommonEnchantability() {
        return 10;
    }

    @Override
    public float getToughness() {
        return 1;
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 520;
            case LEGS:
                return 600;
            case CHEST:
                return 640;
            case HEAD:
                return 440;
        }
        return 0;
    }

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 3;
            case LEGS:
                return 6;
            case CHEST:
                return 7;
            case HEAD:
                return 3;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "steel";
    }

    @Override
    public int getPaxelEnchantability() {
        return 14;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.fromTag(MekanismTags.Items.INGOTS_STEEL);
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}