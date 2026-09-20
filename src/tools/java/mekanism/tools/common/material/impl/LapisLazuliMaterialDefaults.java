package mekanism.tools.common.material.impl;

import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

public class LapisLazuliMaterialDefaults implements BaseMekanismMaterial {

    @Override
    public float axeDamage() {
        return 4;
    }

    @Override
    public float axeAtkSpeed() {
        return -2.9F;
    }

    @Override
    public int durability() {
        return 128;
    }

    @Override
    public float speed() {
        return 9;
    }

    @Override
    public float attackDamageBonus() {
        return 1;
    }

    @Override
    public int enchantmentValue() {
        return 32;
    }

    @Override
    public int armorEnchantmentValue() {
        return enchantmentValue() + 3;
    }

    @Override
    public int shieldDurability() {
        return 224;
    }

    @Override
    public float shieldBlockDelay() {
        return 0.1F;
    }

    @Override
    public float shieldDamageReductionFactor() {
        return 0.8F;
    }

    @Override
    public float shieldDamageThreshold() {
        return 1;
    }

    @Override
    public float toughness() {
        return 0;
    }

    @Override
    public int durability(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 130;
            case LEGGINGS -> 150;
            case CHESTPLATE, BODY -> 160;
            case HELMET -> 110;
        };
    }

    @Override
    public int defense(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS, HELMET -> 1;
            case LEGGINGS -> 3;
            case CHESTPLATE -> 4;
            default -> 0;
        };
    }

    @Override
    public String registryPrefix() {
        return "lapis_lazuli";
    }

    @Override
    public TagKey<Block> incorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_LAPIS_LAZULI_TOOL;
    }

    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_DIAMOND;
    }

    @Override
    public TagKey<Item> repairItems() {
        return Tags.Items.GEMS_LAPIS;
    }

    @Override
    public float spearAttackDuration() {
        return 0.8F;
    }

    @Override
    public float spearDamageMultiplier() {
        return 0.82F;
    }

    @Override
    public float spearDelay() {
        return 0.65F;
    }

    @Override
    public float spearDismountTime() {
        return 4;
    }

    @Override
    public float spearDismountThreshold() {
        return 13;
    }

    @Override
    public float spearKnockbackTime() {
        return 8.75F;
    }

    @Override
    public float spearDamageTime() {
        return 13.25F;
    }
}