package mekanism.tools.common.material.impl;

import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class BronzeMaterialDefaults implements BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 403;
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
    public int getUses() {
        return 375;
    }

    @Override
    public float getSpeed() {
        return 7;
    }

    @Override
    public float getAttackDamageBonus() {
        return 2;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public float toughness() {
        return 1;
    }

    @Override
    public int getDurabilityForType(@NotNull ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS -> 234;
            case LEGGINGS -> 270;
            case CHESTPLATE, BODY -> 288;
            case HELMET -> 198;
        };
    }

    @Override
    public int getDefense(@NotNull ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS -> 2;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 7;
            case HELMET -> 3;
            default -> 0;
        };
    }

    @NotNull
    @Override
    public String getRegistryPrefix() {
        return "bronze";
    }

    @NotNull
    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_BRONZE_TOOL;
    }

    @NotNull
    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @NotNull
    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(MekanismTags.Items.INGOTS_BRONZE);
    }

    @Override
    public float knockbackResistance() {
        return 0;
    }
}