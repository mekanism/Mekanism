package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public class RefinedObsidianMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 1_680;
    }

    @Override
    public float getAxeDamage() {
        return 7;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -2.9F;
    }

    @Override
    public int getUses() {
        return 4_096;
    }

    @Override
    public float getSpeed() {
        return 12;
    }

    @Override
    public float getAttackDamageBonus() {
        return 8;
    }

    @Override
    public int getLevel() {
        return 4;
    }

    @Override
    public int getCommonEnchantability() {
        return 18;
    }

    @Override
    public boolean burnsInFire() {
        return false;
    }

    @Override
    public float getToughness() {
        return 5;
    }

    @Override
    public int getDurabilityForSlot(@Nonnull EquipmentSlot slotType) {
        switch (slotType) {
            case FEET:
                return 975;
            case LEGS:
                return 1_125;
            case CHEST:
                return 1_200;
            case HEAD:
                return 825;
        }
        return 0;
    }

    @Override
    public int getDefenseForSlot(@Nonnull EquipmentSlot slotType) {
        switch (slotType) {
            case FEET:
                return 5;
            case LEGS:
                return 8;
            case CHEST:
                return 12;
            case HEAD:
                return 6;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getConfigCommentName() {
        return "Refined Obsidian";
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "refined_obsidian";
    }

    @Nullable
    @Override
    public Tag<Block> getTag() {
        return ToolsTags.Blocks.NEEDS_REFINED_OBSIDIAN_TOOL;
    }

    @Nonnull
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.of(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN);
    }

    @Override
    public float getKnockbackResistance() {
        return 0.2F;
    }
}