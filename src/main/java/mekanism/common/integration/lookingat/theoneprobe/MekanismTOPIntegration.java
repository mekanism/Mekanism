package mekanism.common.integration.lookingat.theoneprobe;

import mekanism.common.Mekanism;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@Mod(Mekanism.MODID)
public class MekanismTOPIntegration {

    public MekanismTOPIntegration(IEventBus modEventBus) {
        if (Mekanism.hooks.theOneProbe.isLoaded()) {
            modEventBus.addListener(InterModEnqueueEvent.class, this::imcQueue);
        }
    }

    private void imcQueue(InterModEnqueueEvent event) {
        Mekanism.hooks.theOneProbe.sendImc("getTheOneProbe", TOPProvider::new);
    }
}