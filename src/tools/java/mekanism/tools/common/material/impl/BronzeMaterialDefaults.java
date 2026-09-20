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

public class BronzeMaterialDefaults implements BaseMekanismMaterial {

    @Override
    public float axeDamage() {
        return 7;
    }

    @Override
    public float axeAtkSpeed() {
        return -3.0F;
    }

    @Override
    public int durability() {
        return 375;
    }

    @Override
    public float speed() {
        return 7;
    }

    @Override
    public float attackDamageBonus() {
        return 2;
    }

    @Override
    public int enchantmentValue() {
        return 10;
    }

    @Override
    public int shieldDurability() {
        return 403;
    }

    @Override
    public float shieldBlockDelay() {
        return 0.2F;
    }

    @Override
    public float shieldDamageThreshold() {
        return 2.5F;
    }

    @Override
    public float toughness() {
        return 1;
    }

    @Override
    public int durability(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 234;
            case LEGGINGS -> 270;
            case CHESTPLATE, BODY -> 288;
            case HELMET -> 198;
        };
    }

    @Override
    public int defense(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 2;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 7;
            case HELMET -> 3;
            default -> 0;
        };
    }

    @Override
    public String registryPrefix() {
        return "bronze";
    }

    @Override
    public TagKey<Block> incorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_BRONZE_TOOL;
    }

    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public TagKey<Item> repairItems() {
        return MekanismTags.Items.INGOTS_BRONZE;
    }

    @Override
    public float spearAttackDuration() {
        return 0.9F;
    }

    @Override
    public float spearDamageMultiplier() {
        return 0.88F;
    }

    @Override
    public float spearDelay() {
        return 0.65F;
    }

    @Override
    public float spearDismountTime() {
        return 3.25F;
    }

    @Override
    public float spearDismountThreshold() {
        return 11.5F;
    }

    @Override
    public float spearKnockbackTime() {
        return 7.5F;
    }

    @Override
    public float spearDamageTime() {
        return 11.75F;
    }
}