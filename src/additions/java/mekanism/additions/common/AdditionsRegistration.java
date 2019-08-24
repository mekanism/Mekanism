package mekanism.additions.common;

import mekanism.additions.common.entity.AdditionsEntityTypes;
import mekanism.additions.common.tile.AdditionsTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = MekanismAdditions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AdditionsRegistration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        AdditionsBlock.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        AdditionsItem.registerItems(registry);
        AdditionsBlock.registerItemBlocks(registry);
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        AdditionsTileEntityTypes.registerTileEntities(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        AdditionsEntityTypes.registerEntities(event.getRegistry());
    }
}