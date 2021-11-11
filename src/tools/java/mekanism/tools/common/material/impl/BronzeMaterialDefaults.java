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
        return 403;
    }

    @Override
    public float getAxeDamage() {
        return 7;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.0F;
    }

    @Override
    public int getUses() {
        return 375;
    }

    @Override
    public float getSpeed() {
        return 7;
    }

    @Override
    public float getAttackDamageBonus() {
        return 2;
    }

    @Override
    public int getLevel() {
        return 2;
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
    public int getDurabilityForSlot(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 234;
            case LEGS:
                return 270;
            case CHEST:
                return 288;
            case HEAD:
                return 198;
        }
        return 0;
    }

    @Override
    public int getDefenseForSlot(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 2;
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
    public String getConfigCommentName() {
        return "Bronze";
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "bronze";
    }

    @Nonnull
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.of(MekanismTags.Items.INGOTS_BRONZE);
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}