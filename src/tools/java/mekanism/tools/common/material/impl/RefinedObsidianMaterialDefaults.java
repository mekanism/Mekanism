package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import mekanism.common.MekanismItem;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class RefinedObsidianMaterialDefaults extends BaseMekanismMaterial {

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
        return -2;
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
        //TODO: Used to be 3000 for paxel
        return 2_500;
    }

    @Override
    public float getEfficiency() {
        //TODO: Used to be 25 for paxel
        return 20;
    }

    @Override
    public float getAttackDamage() {
        return 10;
    }

    @Override
    public int getHarvestLevel() {
        return 3;
    }

    @Override
    public int getCommonEnchantability() {
        //TODO: Used to be 50 for paxel
        return 40;
    }

    @Override
    public float getToughness() {
        return 4;
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 650;
            case LEGS:
                return 750;
            case CHEST:
                return 800;
            case HEAD:
                return 550;
        }
        return 0;
    }

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 5;
            case LEGS:
                return 8;
            case CHEST:
                return 12;
            case HEAD:
                return 5;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "refined_obsidian";
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.fromItems(MekanismItem.REFINED_OBSIDIAN_INGOT);
    }

    @Nonnull
    @Override
    public String getName() {
        return getRegistryPrefix();
    }
}