package mekanism.tools.common.material.impl;

import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class LapisLazuliMaterialDefaults implements BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 224;
    }

    @Override
    public float getAxeDamage() {
        return 4;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -2.9F;
    }

    @Override
    public int getUses() {
        return 128;
    }

    @Override
    public float getSpeed() {
        return 9;
    }

    @Override
    public float getAttackDamageBonus() {
        return 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 32;
    }

    @Override
    public float toughness() {
        return 0;
    }

    @Override
    public int getDurabilityForType(@NotNull ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS -> 130;
            case LEGGINGS -> 150;
            case CHESTPLATE, BODY -> 160;
            case HELMET -> 110;
        };
    }

    @Override
    public int getDefense(@NotNull ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS, HELMET -> 1;
            case LEGGINGS -> 3;
            case CHESTPLATE -> 4;
            default -> 0;
        };
    }

    @NotNull
    @Override
    public String getRegistryPrefix() {
        return "lapis_lazuli";
    }

    @NotNull
    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_LAPIS_LAZULI_TOOL;
    }

    @NotNull
    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_DIAMOND;
    }

    @NotNull
    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Tags.Items.GEMS_LAPIS);
    }

    @Override
    public float knockbackResistance() {
        return 0;
    }
}