package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.Tags;

public class LapisLazuliMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 582;
    }

    @Override
    public float getAxeDamage() {
        return 6;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.1F;
    }

    @Override
    public float getPaxelDamage() {
        return 6;
    }

    @Override
    public int getPaxelHarvestLevel() {
        return 2;
    }

    @Override
    public int getPaxelMaxUses() {
        return 250;
    }

    @Override
    public float getPaxelEfficiency() {
        return 6;
    }

    @Override
    public int getMaxUses() {
        return 200;
    }

    @Override
    public float getEfficiency() {
        return 5;
    }

    @Override
    public float getAttackDamage() {
        return 2;
    }

    @Override
    public int getHarvestLevel() {
        return 2;
    }

    @Override
    public int getCommonEnchantability() {
        return 8;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 169;
            case LEGS:
                return 195;
            case CHEST:
                return 208;
            case HEAD:
                return 143;
        }
        return 0;
    }

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 2;
            case LEGS:
                return 6;
            case CHEST:
                return 5;
            case HEAD:
                return 2;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "lapis_lazuli";
    }

    @Override
    public int getPaxelEnchantability() {
        return 10;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.fromTag(Tags.Items.GEMS_LAPIS);
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}