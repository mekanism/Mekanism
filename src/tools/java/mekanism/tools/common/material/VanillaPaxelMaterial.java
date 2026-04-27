package mekanism.tools.common.material;

import mekanism.api.annotations.MethodsAreNotNullByDefault;
import net.minecraft.world.item.ToolMaterial;

@MethodsAreNotNullByDefault
public abstract class VanillaPaxelMaterial implements IPaxelMaterial {

    private final String vanillaMaterialName;

    protected VanillaPaxelMaterial(String vanillaMaterialName) {
        this.vanillaMaterialName = vanillaMaterialName;
    }

    public abstract ToolMaterial getVanillaTier();

    public String getRegistryPrefix() {
        return vanillaMaterialName;
    }

    @Override
    public int getPaxelDurability() {
        return 2 * getVanillaTier().durability();
    }

    @Override
    public float getPaxelEfficiency() {
        return getVanillaTier().speed();
    }

    @Override
    public int getPaxelEnchantability() {
        return getVanillaTier().enchantmentValue();
    }

    @Override
    public ToolMaterial toToolMaterial() {
        return getVanillaTier();
    }
}