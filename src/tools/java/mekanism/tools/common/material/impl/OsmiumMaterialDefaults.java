package mekanism.tools.common.material.impl;

import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
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

public class OsmiumMaterialDefaults implements BaseMekanismMaterial {

    @Override
    public float axeDamage() {
        return 8;
    }

    @Override
    public float axeAtkSpeed() {
        return -3.3F;
    }

    @Override
    public int durability() {
        return 1_024;
    }

    @Override
    public float speed() {
        return 4;
    }

    @Override
    public float attackDamageBonus() {
        return 4;
    }

    @Override
    public int enchantmentValue() {
        return 14;
    }

    @Override
    public int shieldDurability() {
        return 672;
    }

    @Override
    public float shieldBlockDelay() {
        return 0.5F;
    }

    @Override
    public float shieldDamageThreshold() {
        return 4.5F;
    }

    @Override
    public float toughness() {
        return 3;
    }

    @Override
    public int durability(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 390;
            case LEGGINGS -> 450;
            case CHESTPLATE, BODY -> 480;
            case HELMET -> 330;
        };
    }

    @Override
    public int defense(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
            case HELMET -> 4;
            default -> 0;
        };
    }

    @Override
    public String registryPrefix() {
        return "osmium";
    }

    @Override
    public TagKey<Block> incorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_OSMIUM_TOOL;
    }

    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public TagKey<Item> repairItems() {
        return MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM);
    }

    @Override
    public float knockbackResistance() {
        return 0.1F;
    }

    @Override
    public float spearAttackDuration() {
        return 1.15F;
    }

    @Override
    public float spearDamageMultiplier() {
        return 1.2F;
    }

    @Override
    public float spearDelay() {
        return 0.7F;
    }

    @Override
    public float spearDismountTime() {
        return 2.5F;
    }

    @Override
    public float spearDismountThreshold() {
        return 8.25F;
    }

    @Override
    public float spearKnockbackTime() {
        return 5.5F;
    }

    @Override
    public float spearDamageTime() {
        return 8.5F;
    }
}