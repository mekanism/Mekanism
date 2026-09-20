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

    int durability();

    float speed();

    int enchantmentValue();

    TagKey<Item> repairItems();
    //end from ToolMaterial

    default float swordDamage() {
        return 3;
    }

    default float swordAtkSpeed() {
        return -2.4F;
    }

    default float shovelDamage() {
        return 1.5F;
    }

    default float shovelAtkSpeed() {
        return -3.0F;
    }

    float axeDamage();

    float axeAtkSpeed();

    default float pickaxeDamage() {
        return 1;
    }

    default float pickaxeAtkSpeed() {
        return -2.8F;
    }

    float attackDamageBonus();

    default float hoeDamage() {
        //Default to match the vanilla hoe's implementation of being negative the attack damage of the material
        return -attackDamageBonus();
    }

    default float hoeAtkSpeed() {
        return attackDamageBonus() - 3.0F;
    }

    @Override
    default float paxelDamage() {
        return axeDamage() + 1;
    }

    @Override
    default int paxelDurability() {
        return 2 * durability();
    }

    @Override
    default float paxelEfficiency() {
        return speed();
    }

    @Override
    default int paxelEnchantability() {
        return enchantmentValue();
    }

    float spearAttackDuration();

    float spearDamageMultiplier();

    float spearDelay();

    float spearDismountTime();

    float spearDismountThreshold();

    float spearKnockbackTime();

    default float spearKnockbackThreshold() {
        return 5.1F;
    }

    float spearDamageTime();

    default float spearDamageThreshold() {
        return 4.6F;
    }

    String registryPrefix();

    default boolean burnsInFire() {
        return true;
    }

    int shieldDurability();

    default float shieldBlockDelay() {
        return 0.25F;
    }

    default float shieldDisableCooldownScale() {
        return 1;
    }

    default float shieldHorizontalBlockingAngle() {
        return 90;
    }

    default float shieldDamageReductionBase() {
        return 0;
    }

    default float shieldDamageReductionFactor() {
        return 1;
    }

    default float shieldDamageThreshold() {
        return 3;
    }

    default float shieldItemDamageBase() {
        return 1;
    }

    default float shieldItemDamageFactor() {
        return 1;
    }

    //Armor material related helpers
    float toughness();

    default float knockbackResistance() {
        return 0;
    }

    default int armorEnchantmentValue() {
        return enchantmentValue() - 5;
    }

    Holder<SoundEvent> equipSound();

    int defense(ArmorType type);

    int durability(ArmorType type);

    default ToolMaterial toToolMaterial() {
        return new ToolMaterial(incorrectBlocksForDrops(), durability(), speed(), attackDamageBonus(), enchantmentValue(), repairItems());
    }

    @Override
    default ToolMaterial toPaxelToolMaterial() {
        return new ToolMaterial(incorrectBlocksForDrops(), paxelDurability(), speed(), attackDamageBonus(), paxelEnchantability(), repairItems());
    }
}