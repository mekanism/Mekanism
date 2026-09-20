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

    default float paxelDisableBlockingSeconds() {
        //Mirror the default value that an axe has
        return 5;
    }

    ToolMaterial toPaxelToolMaterial();
}