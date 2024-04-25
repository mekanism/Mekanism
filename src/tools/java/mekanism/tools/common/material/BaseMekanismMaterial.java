package mekanism.tools.common.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

//TODO - 1.20.5: Replace these with registry entries
@MethodsReturnNonnullByDefault
public interface BaseMekanismMaterial extends /*ArmorMaterial,*/ Tier, IPaxelMaterial {

    @Nullable
    //@Override
    TagKey<Block> getTag();//Force this to be implemented

    //TODO - 1.20.5: Remove and replace these
    //Methods that exist on ArmorMaterial or used to
    float toughness();
    float knockbackResistance();
    Holder<SoundEvent> equipSound();
    int getDefense(ArmorItem.Type type);
    int getDurabilityForType(ArmorItem.Type type);
    @Override//TODO - 1.20.5: Implement properly and figure out what this is even used for
    default TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    }


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
    default int getPaxelMaxUses() {
        return 2 * getUses();
    }

    @Override
    default float getPaxelEfficiency() {
        return getSpeed();
    }

    @Override
    default int getPaxelEnchantability() {
        return getEnchantmentValue();
    }

    String getRegistryPrefix();

    default boolean burnsInFire() {
        return true;
    }
}