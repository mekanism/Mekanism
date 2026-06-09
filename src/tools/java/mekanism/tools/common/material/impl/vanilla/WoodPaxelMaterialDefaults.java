package mekanism.tools.common.material.impl.vanilla;

import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.ToolMaterial;

public class WoodPaxelMaterialDefaults extends VanillaPaxelMaterial {

    public WoodPaxelMaterialDefaults() {
        super("wood");
    }

    @Override
    public ToolMaterial getVanillaTier() {
        return ToolMaterial.WOOD;
    }

    @Override
    public float getPaxelDamage() {
        return 7;
    }
}