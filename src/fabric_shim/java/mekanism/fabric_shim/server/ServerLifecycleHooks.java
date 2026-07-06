package mekanism.fabric_shim.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks the running server (stand-in for net.neoforged.neoforge.server.ServerLifecycleHooks),
 * backed by Fabric's server lifecycle events. {@link #init()} is called once from the Fabric
 * bootstrap.
 */
public final class ServerLifecycleHooks {

    private ServerLifecycleHooks() {
    }

    @Nullable
    private static volatile MinecraftServer currentServer;
    private static boolean initialized;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
