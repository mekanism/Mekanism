package mekanism.tools.common.material.impl.vanilla;

import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.ToolMaterial;

public class GoldPaxelMaterialDefaults extends VanillaPaxelMaterial {

    public GoldPaxelMaterialDefaults() {
        super("gold");
    }

    @Override
    public ToolMaterial getVanillaTier() {
        return ToolMaterial.GOLD;
    }

    @Override
    public float getPaxelDamage() {
        return 7;
    }
}