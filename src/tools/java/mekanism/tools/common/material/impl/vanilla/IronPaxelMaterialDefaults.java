package mekanism.tools.common.material.impl.vanilla;

import javax.annotation.Nonnull;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.Tiers;

public class IronPaxelMaterialDefaults extends VanillaPaxelMaterial {

    @Nonnull
    @Override
    public Tiers getVanillaTier() {
        return Tiers.IRON;
    }

    @Override
    public float getPaxelDamage() {
        return 7;
    }

    @Override
    public String getConfigCommentName() {
        return "Iron";
    }
}