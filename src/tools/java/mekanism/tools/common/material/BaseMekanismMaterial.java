package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import net.minecraft.item.crafting.Ingredient;

//TODO: Set the proper defaults, as I think some numbers changed in 1.14
//TODO: Both IItemTier and IArmorMaterial have get getEnchantability and getRepairMaterial
// This makes it not work properly outside of a dev environment as they have different obfuscated names
public abstract class BaseMekanismMaterial extends IItemTierHelper implements IArmorMaterialHelper {

    public abstract int getSwordDamage();

    public abstract float getSwordAtkSpeed();

    public abstract float getShovelDamage();

    public abstract float getShovelAtkSpeed();

    public abstract float getAxeDamage();

    public abstract float getAxeAtkSpeed();

    public abstract int getPickaxeDamage();

    public abstract float getPickaxeAtkSpeed();

    public abstract float getHoeAtkSpeed();

    public abstract float getPaxelDamage();

    public abstract float getPaxelAtkSpeed();

    public abstract int getPaxelHarvestLevel();

    @Nonnull
    public abstract String getRegistryPrefix();

    //Recombine the methods that are split in such a way as to make it so the compiler can reobfuscate them properly
    public abstract int getCommonEnchantability();

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
}