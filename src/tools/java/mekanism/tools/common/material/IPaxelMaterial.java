package mekanism.tools.common.material;

import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public interface IPaxelMaterial {

    float getPaxelDamage();

    default float getPaxelAtkSpeed() {
        return -2.4F;
    }

    int getPaxelDurability();

    float getPaxelEfficiency();

    int getPaxelEnchantability();
}