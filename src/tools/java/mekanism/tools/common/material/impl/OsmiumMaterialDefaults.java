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
    public float getAxeDamage() {
        return 8;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.3F;
    }

    @Override
    public int getDurability() {
        return 1_024;
    }

    @Override
    public float getSpeed() {
        return 4;
    }

    @Override
    public float getAttackDamageBonus() {
        return 4;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public int getShieldDurability() {
        return 672;
    }

    @Override
    public float getShieldBlockDelay() {
        return 0.5F;
    }

    @Override
    public float getShieldDamageThreshold() {
        return 4.5F;
    }

    @Override
    public float toughness() {
        return 3;
    }

    @Override
    public int getDurabilityForType(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 390;
            case LEGGINGS -> 450;
            case CHESTPLATE, BODY -> 480;
            case HELMET -> 330;
        };
    }

    @Override
    public int getDefense(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
            case HELMET -> 4;
            default -> 0;
        };
    }

    @Override
    public String getRegistryPrefix() {
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
    public TagKey<Item> getRepairItems() {
        return MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM);
    }

    @Override
    public float knockbackResistance() {
        return 0.1F;
    }

    @Override
    public float getSpearAttackDuration() {
        return 1.15F;
    }

    @Override
    public float getSpearDamageMultiplier() {
        return 1.2F;
    }

    @Override
    public float getSpearDelay() {
        return 0.7F;
    }

    @Override
    public float getSpearDismountTime() {
        return 2.5F;
    }

    @Override
    public float getSpearDismountThreshold() {
        return 8.25F;
    }

    @Override
    public float getSpearKnockbackTime() {
        return 5.5F;
    }

    @Override
    public float getSpearDamageTime() {
        return 8.5F;
    }
}