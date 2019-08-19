package mekanism.generators.common;

import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = MekanismGenerators.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GeneratorsRegistration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        GeneratorsBlock.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        GeneratorsItem.registerItems(registry);
        GeneratorsBlock.registerItemBlocks(registry);
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        GeneratorsTileEntityTypes.registerTileEntities(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        GeneratorsContainerTypes.registerContainers(event.getRegistry());
    }
}