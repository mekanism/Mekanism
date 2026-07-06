package mekanism.fabric_shim.internal;

import mekanism.fabric_shim.fml.event.IModBusEvent;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;

/**
 * Internal to the Fabric port (no NeoForge counterpart): owns the per-mod event bus that NeoForge's
 * FML would normally construct and hand to the mod constructor. The Fabric bootstrap passes this to
 * the loader-neutral Mekanism init and then drives the lifecycle (registry events, setup, ...).
 */
public final class ShimBuses {

    private ShimBuses() {
    }

    public static final IEventBus MOD_BUS = BusBuilder.builder()
          .markerType(IModBusEvent.class)
          .build();
}
