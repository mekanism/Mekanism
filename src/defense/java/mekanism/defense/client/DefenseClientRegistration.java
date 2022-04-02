package mekanism.defense.client;

import mekanism.defense.common.MekanismDefense;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekanismDefense.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DefenseClientRegistration {

    private DefenseClientRegistration() {
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
    }
}