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

public class RefinedGlowstoneMaterialDefaults implements BaseMekanismMaterial {

    @Override
    public float axeDamage() {
        return 6;
    }

    @Override
    public float axeAtkSpeed() {
        return -2.9F;
    }

    @Override
    public int durability() {
        return 384;
    }

    @Override
    public float speed() {
        return 15;
    }

    @Override
    public float attackDamageBonus() {
        return 2;
    }

    @Override
    public int enchantmentValue() {
        return 20;
    }

    @Override
    public int armorEnchantmentValue() {
        return enchantmentValue() + 3;
    }

    @Override
    public int shieldDurability() {
        return 381;
    }

    @Override
    public float shieldBlockDelay() {
        return 0.15F;
    }

    @Override
    public float shieldDamageReductionFactor() {
        return 0.9F;
    }

    @Override
    public float shieldDamageThreshold() {
        return 2F;
    }

    @Override
    public float toughness() {
        return 0;
    }

    @Override
    public int durability(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 221;
            case LEGGINGS -> 255;
            case CHESTPLATE, BODY -> 272;
            case HELMET -> 187;
        };
    }

    @Override
    public int defense(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS, HELMET -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
            default -> 0;
        };
    }

    @Override
    public String registryPrefix() {
        return "refined_glowstone";
    }

    @Override
    public TagKey<Block> incorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_REFINED_GLOWSTONE_TOOL;
    }

    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public TagKey<Item> repairItems() {
        return MekanismTags.Items.INGOTS_REFINED_GLOWSTONE;
    }

    @Override
    public float spearAttackDuration() {
        return 0.95F;
    }

    @Override
    public float spearDamageMultiplier() {
        return 0.95F;
    }

    @Override
    public float spearDelay() {
        return 0.6F;
    }

    @Override
    public float spearDismountTime() {
        return 3;
    }

    @Override
    public float spearDismountThreshold() {
        return 12;
    }

    @Override
    public float spearKnockbackTime() {
        return 7.5F;
    }

    @Override
    public float spearDamageTime() {
        return 12.5F;
    }
}