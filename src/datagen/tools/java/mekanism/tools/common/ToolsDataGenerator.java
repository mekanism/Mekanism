package mekanism.tools.common;

import mekanism.tools.client.ToolsLangGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = MekanismTools.MODID, bus = Bus.MOD)
public class ToolsDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new ToolsLangGenerator(gen));
        }
        if (event.includeServer()) {
            //Server side data generators
            gen.addProvider(new ToolsRecipeGenerator(gen));
        }
    }
}