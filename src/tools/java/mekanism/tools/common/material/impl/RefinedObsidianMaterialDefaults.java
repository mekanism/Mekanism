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

public class RefinedObsidianMaterialDefaults implements BaseMekanismMaterial {

    @Override
    public float axeDamage() {
        return 7;
    }

    @Override
    public float axeAtkSpeed() {
        return -2.9F;
    }

    @Override
    public int durability() {
        return 4_096;
    }

    @Override
    public float speed() {
        return 12;
    }

    @Override
    public float attackDamageBonus() {
        return 8;
    }

    @Override
    public int enchantmentValue() {
        return 18;
    }

    @Override
    public boolean burnsInFire() {
        return false;
    }

    @Override
    public int shieldDurability() {
        return 1_680;
    }

    @Override
    public float shieldBlockDelay() {
        return 0.6F;
    }

    @Override
    public float shieldDamageThreshold() {
        return 6;
    }

    @Override
    public float toughness() {
        return 5;
    }

    @Override
    public int durability(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 975;
            case LEGGINGS -> 1_125;
            case CHESTPLATE, BODY -> 1_200;
            case HELMET -> 825;
        };
    }

    @Override
    public int defense(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 5;
            case LEGGINGS -> 8;
            case CHESTPLATE -> 12;
            case HELMET -> 6;
            default -> 0;
        };
    }

    @Override
    public String registryPrefix() {
        return "refined_obsidian";
    }

    @Override
    public TagKey<Block> incorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_REFINED_OBSIDIAN_TOOL;
    }

    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public TagKey<Item> repairItems() {
        return MekanismTags.Items.INGOTS_REFINED_OBSIDIAN;
    }

    @Override
    public float knockbackResistance() {
        return 0.2F;
    }

    @Override
    public float spearAttackDuration() {
        return 1.55F;
    }

    @Override
    public float spearDamageMultiplier() {
        return 1.7F;
    }

    @Override
    public float spearDelay() {
        return 0.2F;
    }

    @Override
    public float spearDismountTime() {
        return 1.5F;
    }

    @Override
    public float spearDismountThreshold() {
        return 5;
    }

    @Override
    public float spearKnockbackTime() {
        return 2.75F;
    }

    @Override
    public float spearDamageTime() {
        return 3.75F;
    }
}