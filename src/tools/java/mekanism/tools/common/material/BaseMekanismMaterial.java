package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import mekanism.tools.common.MekanismTools;
import net.minecraft.item.crafting.Ingredient;

public abstract class BaseMekanismMaterial extends IItemTierHelper implements IArmorMaterialHelper {

    public abstract int getShieldDurability();

    public int getSwordDamage() {
        return 3;
    }

    public float getSwordAtkSpeed() {
        return -2.4F;
    }

    public float getShovelDamage() {
        return 1.5F;
    }

    public float getShovelAtkSpeed() {
        return -3.0F;
    }

    public abstract float getAxeDamage();

    public abstract float getAxeAtkSpeed();

    public int getPickaxeDamage() {
        return 1;
    }

    public float getPickaxeAtkSpeed() {
        return -2.8F;
    }

    public int getHoeDamage() {
        //Default to match the vanilla hoe's implementation of being negative the attack damage of the material
        return (int) -getAttackDamage();
    }

    public float getHoeAtkSpeed() {
        return getAttackDamage() - 3.0F;
    }

    public abstract float getPaxelDamage();

    public float getPaxelAtkSpeed() {
        return -2.4F;
    }

    public abstract int getPaxelHarvestLevel();

    public abstract int getPaxelMaxUses();

    public abstract float getPaxelEfficiency();

    @Nonnull
    public abstract String getRegistryPrefix();

    public abstract int getPaxelEnchantability();

    //Recombine the methods that are split in such a way as to make it so the compiler can reobfuscate them properly
    public abstract int getCommonEnchantability();

    public boolean burnsInFire() {
        return true;
    }

    @Override
    public int getItemEnchantability() {
        return getCommonEnchantability();
    }

    @Override
    public int getArmorEnchantability() {
        return getCommonEnchantability();
    }

    @Nonnull
    public abstract Ingredient getCommonRepairMaterial();

    @Nonnull
    @Override
    public Ingredient getItemRepairMaterial() {
        return getCommonRepairMaterial();
    }

    @Nonnull
    @Override
    public Ingredient getArmorRepairMaterial() {
        return getCommonRepairMaterial();
    }

    @Nonnull
    @Override
    public String getName() {
        return MekanismTools.MODID + ":" + getRegistryPrefix();
    }
}