package mekanism.fabric_shim.fml;

/**
 * Logical side of game logic (stand-in for net.neoforged.fml.LogicalSide; same surface).
 */
public enum LogicalSide {
    CLIENT,
    SERVER;

    public boolean isServer() {
        return this == SERVER;
    }

    public boolean isClient() {
        return this == CLIENT;
    }
}
