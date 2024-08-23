package mekanism.tools.common.material;

import java.util.Locale;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Tiers;

@MethodsReturnNonnullByDefault
public abstract class VanillaPaxelMaterial implements IPaxelMaterial {

    public abstract Tiers getVanillaTier();

    public String getRegistryPrefix() {
        return getVanillaTier().name().toLowerCase(Locale.ROOT);
    }

    @Override
    public int getPaxelDurability() {
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