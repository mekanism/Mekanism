package mekanism.tools.common.material;

import net.minecraft.world.item.ToolMaterial;

public interface IPaxelMaterial {

    float getPaxelDamage();

    default float getPaxelAtkSpeed() {
        return -2.4F;
    }

    int getPaxelDurability();

    float getPaxelEfficiency();

    int getPaxelEnchantability();

    ToolMaterial toToolMaterial();
}