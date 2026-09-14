package mekanism.tools.common.material.impl;

import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

public class SteelMaterialDefaults implements BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 448;
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
    public int getDurability() {
        return 500;
    }

    @Override
    public float getSpeed() {
        return 8;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3;
    }

    @Override
    public int getEnchantmentValue() {
        return 16;
    }

    @Override
    public float toughness() {
        return 2;
    }

    @Override
    public int getDurabilityForType(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 260;
            case LEGGINGS -> 300;
            case CHESTPLATE, BODY -> 320;
            case HELMET -> 220;
        };
    }

    @Override
    public int getDefense(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS, HELMET -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
            default -> 0;
        };
    }

    @Override
    public String getRegistryPrefix() {
        return "steel";
    }

    @Override
    public TagKey<Block> incorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_STEEL_TOOL;
    }

    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public TagKey<Item> getRepairItems() {
        return MekanismTags.Items.INGOTS_STEEL;
    }

    @Override
    public float getSpearAttackDuration() {
        return 1.05F;
    }

    @Override
    public float getSpearDamageMultiplier() {
        return 1.075F;
    }

    @Override
    public float getSpearDelay() {
        return 0.55F;
    }

    @Override
    public float getSpearDismountTime() {
        return 2.75F;
    }

    @Override
    public float getSpearDismountThreshold() {
        return 10.5F;
    }

    @Override
    public float getSpearKnockbackTime() {
        return 6.5F;
    }

    @Override
    public float getSpearDamageTime() {
        return 10.5F;
    }
}