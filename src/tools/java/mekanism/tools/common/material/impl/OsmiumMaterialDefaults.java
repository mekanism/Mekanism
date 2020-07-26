package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class OsmiumMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 1_344;
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
        return 5;
    }

    @Override
    public int getPaxelHarvestLevel() {
        return 3;
    }

    @Override
    public int getPaxelMaxUses() {
        return 700;
    }

    @Override
    public float getPaxelEfficiency() {
        return 12;
    }

    @Override
    public int getMaxUses() {
        return 500;
    }

    @Override
    public float getEfficiency() {
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

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
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

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "osmium";
    }

    @Override
    public int getPaxelEnchantability() {
        return 16;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.fromTag(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM));
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}