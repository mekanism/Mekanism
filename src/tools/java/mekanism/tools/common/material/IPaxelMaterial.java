package mekanism.tools.common.material;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Weapon;

public interface IPaxelMaterial {

    float paxelDamage();

    default float paxelAtkSpeed() {
        return -2.4F;
    }

    int paxelDurability();

    float paxelEfficiency();

    int paxelEnchantability();

    default float paxelDisableBlockingSeconds() {
        return Weapon.AXE_DISABLES_BLOCKING_FOR_SECONDS;
    }

    ToolMaterial toPaxelToolMaterial();
}