package mekanism.tools.common.material.impl.vanilla;

import javax.annotation.Nonnull;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.item.ItemTier;

public class DiamondPaxelMaterialDefaults extends VanillaPaxelMaterial {

    @Nonnull
    @Override
    public ItemTier getVanillaTier() {
        return ItemTier.DIAMOND;
    }

    @Override
    public float getPaxelDamage() {
        return 6;
    }

    @Override
    public String getConfigCommentName() {
        return "Diamond";
    }
}