package mekanism.fabric;

import mekanism.fabric_shim.internal.ShimBuses;
import mekanism.fabric_shim.registries.ShimRegistryEvents;
import mekanism.fabric_shim.server.ServerLifecycleHooks;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric bootstrap entry point. Mirrors what NeoForge's FML does around the {@code @Mod}
 * constructor: construct the mod (which subscribes its DeferredRegisters and lifecycle listeners to
 * the mod bus), then drive the lifecycle events in FML's order.
 *
 * <p>Phase 1 (in progress): the shim lifecycle runs; {@code mekanism.common.Mekanism}'s
 * loader-neutral init is constructed here once the entry-point split lands (see PORTING.md).
 */
public class MekanismFabric implements ModInitializer {

    public static final String MODID = "mekanism";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mekanism");

    @Override
    public void onInitialize() {
        LOGGER.info("Mekanism Fabric bootstrap starting (Phase 1)");
        //Harmless if already started; buses only begin shut down when built with startShutdown()
        ShimBuses.MOD_BUS.start();
        ServerLifecycleHooks.init();

        //Mod construction goes here: new Mekanism(...) equivalent subscribing to ShimBuses.MOD_BUS
        // (lands with the entry-point split; see PORTING.md Phase 1)

        //Registration lifecycle: NewRegistryEvent, then RegisterEvent per registry in NeoForge's order.
        //Must happen inside onInitialize while Fabric still permits Registry.register.
        ShimRegistryEvents.fire(ShimBuses.MOD_BUS);
        LOGGER.info("Mekanism Fabric bootstrap complete (registry lifecycle fired)");
    }
}
