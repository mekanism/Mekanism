package mekanism.tools.common.material.impl.vanilla;

import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Tiers;

@MethodsReturnNonnullByDefault
public class DiamondPaxelMaterialDefaults extends VanillaPaxelMaterial {

    @Override
    public Tiers getVanillaTier() {
        return Tiers.DIAMOND;
    }

    @Override
    public float getPaxelDamage() {
        return 6;
    }
}