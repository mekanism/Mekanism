package mekanism.tools.common.material;

import mekanism.api.annotations.MethodsAreNotNullByDefault;

@MethodsAreNotNullByDefault
public interface IPaxelMaterial {

    float getPaxelDamage();

    default float getPaxelAtkSpeed() {
        return -2.4F;
    }

    int getPaxelDurability();

    float getPaxelEfficiency();

    int getPaxelEnchantability();
}