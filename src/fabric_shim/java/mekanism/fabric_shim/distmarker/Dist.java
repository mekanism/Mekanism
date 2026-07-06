package mekanism.fabric_shim.distmarker;

/**
 * Physical side the game is running as (stand-in for net.neoforged.api.distmarker.Dist,
 * backed by Fabric's EnvType).
 */
public enum Dist {
    CLIENT,
    DEDICATED_SERVER;

    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isDedicatedServer() {
        return this == DEDICATED_SERVER;
    }
}
