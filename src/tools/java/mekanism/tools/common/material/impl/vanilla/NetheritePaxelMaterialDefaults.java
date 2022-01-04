package mekanism.tools.common.material.impl.vanilla;

import javax.annotation.Nonnull;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.Tiers;

public class NetheritePaxelMaterialDefaults extends VanillaPaxelMaterial {

    @Nonnull
    @Override
    public Tiers getVanillaTier() {
        return Tiers.NETHERITE;
    }

    @Override
    public float getPaxelDamage() {
        return 6;
    }

    @Override
    public String getConfigCommentName() {
        return "Netherite";
    }
}