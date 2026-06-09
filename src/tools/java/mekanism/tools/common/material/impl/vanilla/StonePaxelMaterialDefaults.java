package mekanism.tools.common.material.impl.vanilla;

import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.ToolMaterial;

public class StonePaxelMaterialDefaults extends VanillaPaxelMaterial {

    public StonePaxelMaterialDefaults() {
        super("stone");
    }

    @Override
    public ToolMaterial getVanillaTier() {
        return ToolMaterial.STONE;
    }

    @Override
    public float getPaxelDamage() {
        return 8;
    }
}