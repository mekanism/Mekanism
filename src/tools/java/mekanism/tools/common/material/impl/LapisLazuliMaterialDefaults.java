package mekanism.tools.common.material.impl;

import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LapisLazuliMaterialDefaults extends BaseMekanismMaterial {

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
    public int getLevel() {
        return 1;
    }

    @Override
    public int getCommonEnchantability() {
        return 32;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public int getDurabilityForSlot(@NotNull EquipmentSlot slotType) {
        return switch (slotType) {
            case FEET -> 130;
            case LEGS -> 150;
            case CHEST -> 160;
            case HEAD -> 110;
            default -> 0;
        };
    }

    @Override
    public int getDefenseForSlot(@NotNull EquipmentSlot slotType) {
        return switch (slotType) {
            case FEET -> 1;
            case LEGS -> 3;
            case CHEST -> 4;
            case HEAD -> 1;
            default -> 0;
        };
    }

    @NotNull
    @Override
    public String getConfigCommentName() {
        return "Lapis Lazuli";
    }

    @NotNull
    @Override
    public String getRegistryPrefix() {
        return "lapis_lazuli";
    }

    @Nullable
    @Override
    public TagKey<Block> getTag() {
        return ToolsTags.Blocks.NEEDS_LAPIS_LAZULI_TOOL;
    }

    @NotNull
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_DIAMOND;
    }

    @NotNull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.of(Tags.Items.GEMS_LAPIS);
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}