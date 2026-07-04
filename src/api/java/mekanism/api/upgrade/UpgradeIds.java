package mekanism.api.upgrade;

import mekanism.api.MekanismAPI;
import mekanism.api.MekanismRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/// @since 10.8.0
public class UpgradeIds {

    private UpgradeIds() {
    }

    public static final ResourceKey<Upgrade> ANCHOR = key("anchor");
    public static final ResourceKey<Upgrade> CHEMICAL = key("chemical");
    public static final ResourceKey<Upgrade> ENERGY = key("energy");
    public static final ResourceKey<Upgrade> FILTER = key("filter");
    public static final ResourceKey<Upgrade> MUFFLING = key("muffling");
    public static final ResourceKey<Upgrade> SPEED = key("speed");
    public static final ResourceKey<Upgrade> STONE_GENERATOR = key("stone_generator");

    private static ResourceKey<Upgrade> key(Identifier id) {
        return ResourceKey.create(MekanismRegistries.Keys.UPGRADES, id);
    }

    private static ResourceKey<Upgrade> key(String name) {
        return key(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, name));
    }
}