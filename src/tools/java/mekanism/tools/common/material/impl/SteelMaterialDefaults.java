package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public class SteelMaterialDefaults extends BaseMekanismMaterial {

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
    public int getUses() {
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
    public int getLevel() {
        return 3;
    }

    @Override
    public int getCommonEnchantability() {
        return 16;
    }

    @Override
    public float getToughness() {
        return 2;
    }

    @Override
    public int getDurabilityForSlot(@Nonnull EquipmentSlot slotType) {
        return switch (slotType) {
            case FEET -> 260;
            case LEGS -> 300;
            case CHEST -> 320;
            case HEAD -> 220;
            default -> 0;
        };
    }

    @Override
    public int getDefenseForSlot(@Nonnull EquipmentSlot slotType) {
        return switch (slotType) {
            case FEET -> 3;
            case LEGS -> 6;
            case CHEST -> 8;
            case HEAD -> 3;
            default -> 0;
        };
    }

    @Nonnull
    @Override
    public String getConfigCommentName() {
        return "Steel";
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "steel";
    }

    @Nullable
    @Override
    public TagKey<Block> getTag() {
        return ToolsTags.Blocks.NEEDS_STEEL_TOOL;
    }

    @Nonnull
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.of(MekanismTags.Items.INGOTS_STEEL);
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}