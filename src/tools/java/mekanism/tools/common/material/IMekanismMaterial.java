package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IItemTier;

//TODO: Set the defaults
public interface IMekanismMaterial extends IItemTier, IArmorMaterial {

    int getSwordDamage();

    float getSwordAtkSpeed();

    int getShovelDamage();

    float getShovelAtkSpeed();

    int getAxeDamage();

    float getAxeAtkSpeed();

    int getPickaxeDamage();

    float getPickaxeAtkSpeed();

    float getHoeAtkSpeed();

    int getPaxelHarvestLevel();

    int getPaxelDamage();

    float getPaxelAtkSpeed();

    @Nonnull
    String getRegistryPrefix();
}