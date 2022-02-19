package mekanism.tools.common.material.impl.vanilla;

import javax.annotation.Nonnull;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.item.ItemTier;

public class StonePaxelMaterialDefaults extends VanillaPaxelMaterial {

    @Nonnull
    @Override
    public ItemTier getVanillaTier() {
        return ItemTier.STONE;
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