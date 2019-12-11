package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import mekanism.common.MekanismItem;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class OsmiumMaterialDefaults extends BaseMekanismMaterial {

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
        return 4;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.0F;
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
        //TODO: Used to be 700 for paxel
        return 500;
    }

    @Override
    public float getEfficiency() {
        //TODO: Used to be 12 for paxel
        return 10;
    }

    @Override
    public float getAttackDamage() {
        return 4;
    }

    @Override
    public int getHarvestLevel() {
        return 2;
    }

    @Override
    public int getCommonEnchantability() {
        //TODO: Used to be 16 for paxel
        return 12;
    }

    @Override
    public float getToughness() {
        return 1;
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 3;
            case LEGS:
                return 6;
            case CHEST:
                return 5;
            case HEAD:
                return 3;
        }
        return 0;
    }

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 390;
            case LEGS:
                return 450;
            case CHEST:
                return 480;
            case HEAD:
                return 330;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "osmium";
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.fromItems(MekanismItem.OSMIUM_INGOT);
    }

    @Nonnull
    @Override
    public String getName() {
        return getRegistryPrefix();
    }
}