package mekanism.fabric.client;

import mekanism.fabric.MekanismFabric;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client bootstrap. Phase 4 moves {@code mekanism.client.ClientRegistration}'s
 * mod-bus registrations here (see PORTING.md).
 */
public class MekanismFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MekanismFabric.LOGGER.info("Mekanism Fabric client bootstrap loaded");
    }
}
