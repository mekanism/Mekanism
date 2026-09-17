package mekanism.common.registries;

import mekanism.common.Mekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public class MekanismContextIntProviders {

    private MekanismContextIntProviders() {
    }

    public static final ResourceKey<ContextIntProvider> COOKING_TIME_CHARCOAL_BLOCK = createKey("cooking/time_charcoal_block");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_BIO_FUEL = createKey("cooking/time_bio_fuel");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_BIO_FUEL_BLOCK = createKey("cooking/time_bio_fuel_block");

    private static ResourceKey<ContextIntProvider> createKey(String location) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Mekanism.rl(location));
    }
}