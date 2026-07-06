package mekanism.fabric_shim.fml;

import mekanism.fabric_shim.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Stand-in for net.neoforged.fml.loading.FMLEnvironment, backed by Fabric Loader.
 */
public final class FMLEnvironment {

    private FMLEnvironment() {
    }

    public static final Dist dist = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
    public static final boolean production = !FabricLoader.getInstance().isDevelopmentEnvironment();
}
