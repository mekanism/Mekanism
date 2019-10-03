package mekanism.tools.common;

import mekanism.tools.common.config.MekanismToolsConfig;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MekanismTools.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ToolsRegistration {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        MekanismToolsConfig.loadFromFiles();
        ToolsItem.registerItems(event.getRegistry());
    }
}