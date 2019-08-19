package mekanism.tools.common;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MekanismTools.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ToolsRegistration {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ToolsItem.registerItems(event.getRegistry());
    }
}