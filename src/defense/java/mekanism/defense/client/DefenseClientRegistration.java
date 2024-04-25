package mekanism.defense.client;

import mekanism.defense.common.MekanismDefense;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = MekanismDefense.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class DefenseClientRegistration {

    private DefenseClientRegistration() {
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
    }
}