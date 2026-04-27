package mekanism.tools.common.material.impl.vanilla;

import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.ToolMaterial;

@MethodsAreNotNullByDefault
public class DiamondPaxelMaterialDefaults extends VanillaPaxelMaterial {

    public DiamondPaxelMaterialDefaults() {
        super("diamond");
    }

    @Override
    public ToolMaterial getVanillaTier() {
        return ToolMaterial.DIAMOND;
    }

    @Override
    public float getPaxelDamage() {
        return 6;
    }
}