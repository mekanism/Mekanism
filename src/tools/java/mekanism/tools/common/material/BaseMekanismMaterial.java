package mekanism.tools.common.material;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

public interface BaseMekanismMaterial extends IPaxelMaterial {

    //begin from ToolMaterial
    TagKey<Block> incorrectBlocksForDrops();

    int getDurability();

    float getSpeed();

    int getEnchantmentValue();

    TagKey<Item> getRepairItems();
    //end from ToolMaterial

    int getShieldDurability();

    default float getSwordDamage() {
        return 3;
    }

    default float getSwordAtkSpeed() {
        return -2.4F;
    }

    default float getShovelDamage() {
        return 1.5F;
    }

    default float getShovelAtkSpeed() {
        return -3.0F;
    }

    float getAxeDamage();

    float getAxeAtkSpeed();

    default float getPickaxeDamage() {
        return 1;
    }

    default float getPickaxeAtkSpeed() {
        return -2.8F;
    }

    float getAttackDamageBonus();

    default float getHoeDamage() {
        //Default to match the vanilla hoe's implementation of being negative the attack damage of the material
        return -getAttackDamageBonus();
    }

    default float getHoeAtkSpeed() {
        return getAttackDamageBonus() - 3.0F;
    }

    @Override
    default float getPaxelDamage() {
        return getAxeDamage() + 1;
    }

    @Override
    default int getPaxelDurability() {
        return 2 * getDurability();
    }

    @Override
    default float getPaxelEfficiency() {
        return getSpeed();
    }

    @Override
    default int getPaxelEnchantability() {
        return getEnchantmentValue();
    }

    float getSpearAttackDuration();

    float getSpearDamageMultiplier();

    float getSpearDelay();

    float getSpearDismountTime();

    float getSpearDismountThreshold();

    float getSpearKnockbackTime();

    default float getSpearKnockbackThreshold() {
        return 5.1F;
    }

    float getSpearDamageTime();

    default float getSpearDamageThreshold() {
        return 4.6F;
    }

    String getRegistryPrefix();

    default boolean burnsInFire() {
        return true;
    }

    //Armor material related helpers
    float toughness();

    default float knockbackResistance() {
        return 0;
    }

    default int getArmorEnchantmentValue() {
        return getEnchantmentValue() - 5;
    }

    Holder<SoundEvent> equipSound();

    int getDefense(ArmorType type);

    int getDurabilityForType(ArmorType type);

    @Override
    default ToolMaterial toToolMaterial() {
        return new ToolMaterial(incorrectBlocksForDrops(), getDurability(), getSpeed(), getAttackDamageBonus(), getEnchantmentValue(), getRepairItems());
    }
}