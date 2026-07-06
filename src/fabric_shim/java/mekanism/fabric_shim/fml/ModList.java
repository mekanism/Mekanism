package mekanism.fabric_shim.fml;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Stand-in for net.neoforged.fml.ModList, backed by Fabric Loader.
 *
 * <p>Only the surface Mekanism uses at runtime is provided. FML's annotation scan data
 * (getAllScanData) has no Fabric equivalent and is handled separately where it is consumed
 * (see MekAnnotationScanner porting notes in PORTING.md).
 */
public final class ModList {

    private static final ModList INSTANCE = new ModList();

    private ModList() {
    }

    public static ModList get() {
        return INSTANCE;
    }

    public boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
