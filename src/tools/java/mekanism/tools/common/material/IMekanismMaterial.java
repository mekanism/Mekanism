package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IItemTier;

//TODO: Set the defaults
public interface IMekanismMaterial extends IItemTier, IArmorMaterial {

    int getSwordDamage();

    float getSwordAtkSpeed();

    float getShovelDamage();

    float getShovelAtkSpeed();

    float getAxeDamage();

    float getAxeAtkSpeed();

    int getPickaxeDamage();

    float getPickaxeAtkSpeed();

    float getHoeAtkSpeed();

    float getPaxelDamage();

    float getPaxelAtkSpeed();

    int getPaxelHarvestLevel();

    @Nonnull
    String getRegistryPrefix();
}