package mekanism.tools.common.material.impl.vanilla;

import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.ToolMaterial;

@MethodsAreNotNullByDefault
public class NetheritePaxelMaterialDefaults extends VanillaPaxelMaterial {

    public NetheritePaxelMaterialDefaults() {
        super("netherite");
    }

    @Override
    public ToolMaterial getVanillaTier() {
        return ToolMaterial.NETHERITE;
    }

    @Override
    public float getPaxelDamage() {
        return 6;
    }
}