package mekanism.tools.common.material.impl.vanilla;

import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.ToolMaterial;

@MethodsAreNotNullByDefault
public class IronPaxelMaterialDefaults extends VanillaPaxelMaterial {

    public IronPaxelMaterialDefaults() {
        super("iron");
    }

    @Override
    public ToolMaterial getVanillaTier() {
        return ToolMaterial.IRON;
    }

    @Override
    public float getPaxelDamage() {
        return 7;
    }
}