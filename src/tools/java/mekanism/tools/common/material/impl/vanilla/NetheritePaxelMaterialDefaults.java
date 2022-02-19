package mekanism.tools.common.material.impl.vanilla;

import javax.annotation.Nonnull;
import mekanism.tools.common.material.VanillaPaxelMaterial;
import net.minecraft.item.ItemTier;

public class NetheritePaxelMaterialDefaults extends VanillaPaxelMaterial {

    @Nonnull
    @Override
    public ItemTier getVanillaTier() {
        return ItemTier.NETHERITE;
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