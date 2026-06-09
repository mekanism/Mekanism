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
    public int getShieldDurability() {
        return 381;
    }

    @Override
    public float getAxeDamage() {
        return 6;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -2.9F;
    }

    @Override
    public int getDurability() {
        return 384;
    }

    @Override
    public float getSpeed() {
        return 15;
    }

    @Override
    public float getAttackDamageBonus() {
        return 2;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }

    @Override
    public float toughness() {
        return 0;
    }

    @Override
    public int getDurabilityForType(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 221;
            case LEGGINGS -> 255;
            case CHESTPLATE, BODY -> 272;
            case HELMET -> 187;
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
    public TagKey<Item> getRepairItems() {
        return MekanismTags.Items.INGOTS_REFINED_GLOWSTONE;
    }

    @Override
    public float knockbackResistance() {
        return 0;
    }
}