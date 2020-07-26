package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class RefinedGlowstoneMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 806;
    }

    @Override
    public float getAxeDamage() {
        return 3;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.1F;
    }

    @Override
    public float getPaxelDamage() {
        return 4;
    }

    @Override
    public int getPaxelHarvestLevel() {
        return 3;
    }

    @Override
    public int getPaxelMaxUses() {
        return 450;
    }

    @Override
    public float getPaxelEfficiency() {
        return 18;
    }

    @Override
    public int getMaxUses() {
        return 300;
    }

    @Override
    public float getEfficiency() {
        return 14;
    }

    @Override
    public float getAttackDamage() {
        return 5;
    }

    @Override
    public int getHarvestLevel() {
        return 2;
    }

    @Override
    public int getCommonEnchantability() {
        return 18;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
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
        return "refined_glowstone";
    }

    @Override
    public int getPaxelEnchantability() {
        return 22;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.fromTag(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE);
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}