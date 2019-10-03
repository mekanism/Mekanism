package mekanism.additions.common;

import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.entity.AdditionsEntityType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        AdditionsEntityType.registerEntities(event.getRegistry());
    }

    @SubscribeEvent
    public static void onLoad(ModConfig.Loading configEvent) {
        MekanismAdditionsConfig.loadFromFiles();
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.ConfigReloading configEvent) {
        //TODO: Handle reloading
    }
}