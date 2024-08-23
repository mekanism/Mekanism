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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class OsmiumMaterialDefaults implements BaseMekanismMaterial {

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
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public float toughness() {
        return 3;
    }

    @Override
    public int getDurabilityForType(@NotNull ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS -> 390;
            case LEGGINGS -> 450;
            case CHESTPLATE, BODY -> 480;
            case HELMET -> 330;
        };
    }

    @Override
    public int getDefense(@NotNull ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
            case HELMET -> 4;
            default -> 0;
        };
    }

    @NotNull
    @Override
    public String getRegistryPrefix() {
        return "osmium";
    }

    @NotNull
    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return ToolsTags.Blocks.INCORRECT_FOR_OSMIUM_TOOL;
    }

    @NotNull
    @Override
    public Holder<SoundEvent> equipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @NotNull
    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM));
    }

    @Override
    public float knockbackResistance() {
        return 0.1F;
    }
}