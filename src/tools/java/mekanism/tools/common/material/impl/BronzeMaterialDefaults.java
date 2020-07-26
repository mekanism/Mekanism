package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class BronzeMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 1_568;
    }

    @Override
    public float getAxeDamage() {
        return 2;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.1F;
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
        return 1_100;
    }

    @Override
    public float getPaxelEfficiency() {
        return 16;
    }

    @Override
    public int getMaxUses() {
        return 800;
    }

    @Override
    public float getEfficiency() {
        return 14;
    }

    @Override
    public float getAttackDamage() {
        return 6;
    }

    @Override
    public int getHarvestLevel() {
        return 2;
    }

    @Override
    public int getCommonEnchantability() {
        return 10;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 455;
            case LEGS:
                return 525;
            case CHEST:
                return 560;
            case HEAD:
                return 385;
        }
        return 0;
    }

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 2;
            case LEGS:
                return 5;
            case CHEST:
                return 6;
            case HEAD:
                return 3;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "bronze";
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
        return Ingredient.fromTag(MekanismTags.Items.INGOTS_BRONZE);
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}