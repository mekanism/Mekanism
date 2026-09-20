package mekanism.tools.common.material;

import net.minecraft.world.item.ToolMaterial;

public interface IPaxelMaterial {

    float paxelDamage();

    default float paxelAtkSpeed() {
        return -2.4F;
    }

    int paxelDurability();

    float paxelEfficiency();

    int paxelEnchantability();

    default float paxelDisableBlockingSeconds() {
        //Mirror the default value that an axe has
        return 5;
    }

    ToolMaterial toPaxelToolMaterial();
}