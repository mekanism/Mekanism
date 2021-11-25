package mekanism.tools.common.material;

import java.util.Locale;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemTier;

public abstract class VanillaPaxelMaterial implements IPaxelMaterial {

    @Nonnull
    public abstract ItemTier getVanillaTier();

    @Nonnull
    public String getRegistryPrefix() {
        return getVanillaTier().name().toLowerCase(Locale.ROOT);
    }

    @Override
    public int getPaxelHarvestLevel() {
        return getVanillaTier().getLevel();
    }

    @Override
    public int getPaxelMaxUses() {
        return 2 * getVanillaTier().getUses();
    }

    @Override
    public float getPaxelEfficiency() {
        return getVanillaTier().getSpeed();
    }

    @Override
    public int getPaxelEnchantability() {
        return getVanillaTier().getEnchantmentValue();
    }
}