package mekanism.fabric_shim.common;

import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;

/**
 * Holds the game-wide event bus (stand-in for net.neoforged.neoforge.common.NeoForge).
 * Shim event glue posts to this bus from Fabric callbacks (Phase 3).
 */
public final class NeoForge {

    private NeoForge() {
    }

    public static final IEventBus EVENT_BUS = BusBuilder.builder().build();
}
