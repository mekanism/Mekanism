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
import org.jetbrains.annotations.NotNull;

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
    public int getDurabilityForType(@NotNull ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> 260;
            case LEGGINGS -> 300;
            case CHESTPLATE, BODY -> 320;
            case HELMET -> 220;
        };
    }

    @Override
    public int getDefense(@NotNull ArmorType armorType) {
        return switch (armorType) {
            case BOOTS, HELMET -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
            default -> 0;
        };
    }

    @NotNull
    @Override
    public String getRegistryPrefix() {
        return "steel";
    }

    @NotNull
    @Override
    public TagKey<Block> incorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_STEEL_TOOL;
    }

    @NotNull
    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public TagKey<Item> getRepairItems() {
        return MekanismTags.Items.INGOTS_STEEL;
    }

    @Override
    public float knockbackResistance() {
        return 0;
    }
}