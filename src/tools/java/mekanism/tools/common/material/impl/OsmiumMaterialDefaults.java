package mekanism.tools.common.material.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;

public class OsmiumMaterialDefaults extends BaseMekanismMaterial {

    @Override
    public int getShieldDurability() {
        return 672;
    }

    @Override
    public float getAxeDamage() {
        return 8;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.3F;
    }

    @Override
    public int getUses() {
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
    public int getLevel() {
        return 2;
    }

    @Override
    public int getCommonEnchantability() {
        return 14;
    }

    @Override
    public float getToughness() {
        return 3;
    }

    @Override
    public int getDurabilityForSlot(@Nonnull EquipmentSlot slotType) {
        switch (slotType) {
            case FEET:
                return 390;
            case LEGS:
                return 450;
            case CHEST:
                return 480;
            case HEAD:
                return 330;
        }
        return 0;
    }

    @Override
    public int getDefenseForSlot(@Nonnull EquipmentSlot slotType) {
        switch (slotType) {
            case FEET:
                return 3;
            case LEGS:
                return 6;
            case CHEST:
                return 8;
            case HEAD:
                return 4;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getConfigCommentName() {
        return "Osmium";
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "osmium";
    }

    @Nullable
    @Override
    public Tag<Block> getTag() {
        return ToolsTags.Blocks.NEEDS_OSMIUM_TOOL;
    }

    @Nonnull
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return Ingredient.of(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM));
    }

    @Override
    public float getKnockbackResistance() {
        return 0.1F;
    }
}