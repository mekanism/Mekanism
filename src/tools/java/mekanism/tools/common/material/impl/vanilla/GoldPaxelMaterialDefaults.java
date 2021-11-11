package mekanism.tools.common.material.impl.vanilla;

import javax.annotation.Nonnull;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.item.ItemTier;

public class GoldPaxelMaterialDefaults extends VanillaPaxelMaterial {

    @Nonnull
    @Override
    public ItemTier getVanillaTier() {
        return ItemTier.GOLD;
    }

    @Override
    public float getPaxelDamage() {
        return 7;
    }

    @Override
    public String getConfigCommentName() {
        return "Gold";
    }
}