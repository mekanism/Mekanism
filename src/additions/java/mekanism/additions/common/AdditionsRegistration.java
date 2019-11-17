package mekanism.additions.common;

import mekanism.additions.common.entity.AdditionsEntityType;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = MekanismAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AdditionsRegistration {

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        AdditionsEntityType.registerEntities(event.getRegistry());
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.ConfigReloading configEvent) {
        //TODO: Handle reloading
    }
}