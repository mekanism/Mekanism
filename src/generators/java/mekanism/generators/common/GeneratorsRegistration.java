package mekanism.generators.common;

import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = MekanismGenerators.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneratorsRegistration {

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        GeneratorsTileEntityTypes.registerTileEntities(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        GeneratorsContainerTypes.registerContainers(event.getRegistry());
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.ConfigReloading configEvent) {
        //TODO: Handle reloading
    }
}