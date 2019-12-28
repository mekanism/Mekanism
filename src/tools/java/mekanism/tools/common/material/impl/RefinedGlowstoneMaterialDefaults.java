package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import mekanism.common.registries.MekanismItems;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class RefinedGlowstoneMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getSwordDamage() {
        return 3;
    }

    @Override
    public float getSwordAtkSpeed() {
        return -2.4F;
    }

    @Override
    public float getShovelDamage() {
        return 1.5F;
    }

    @Override
    public float getShovelAtkSpeed() {
        return -3.0F;
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
    public int getPickaxeDamage() {
        return 1;
    }

    @Override
    public float getPickaxeAtkSpeed() {
        return -2.8F;
    }

    @Override
    public float getHoeAtkSpeed() {
        return getAttackDamage() - 3.0F;
    }

    @Override
    public float getPaxelDamage() {
        return 4;
    }

    @Override
    public float getPaxelAtkSpeed() {
        return -2.4F;
    }

    @Override
    public int getPaxelHarvestLevel() {
        return 3;
    }

    @Override
    public int getMaxUses() {
        //TODO: Used to be 450 for paxel
        return 300;
    }

    @Override
    public float getEfficiency() {
        //TODO: Used to be 18 for paxel
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
        //TODO: Used to be 22 for paxel
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

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.fromItems(MekanismItems.REFINED_GLOWSTONE_INGOT);
    }

    @Nonnull
    @Override
    public String getName() {
        return getRegistryPrefix();
    }
}