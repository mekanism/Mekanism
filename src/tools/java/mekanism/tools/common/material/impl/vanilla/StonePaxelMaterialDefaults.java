package mekanism.tools.common.material.impl.vanilla;

import javax.annotation.Nonnull;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.world.item.Tiers;

public class StonePaxelMaterialDefaults extends VanillaPaxelMaterial {

    @Nonnull
    @Override
    public Tiers getVanillaTier() {
        return Tiers.STONE;
    }

    @Override
    public float getPaxelDamage() {
        return 8;
    }

    @Override
    public String getConfigCommentName() {
        return "Stone";
    }
}