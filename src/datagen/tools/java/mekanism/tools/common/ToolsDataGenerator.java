package mekanism.tools.common;

import mekanism.tools.client.ToolsLangProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = MekanismTools.MODID, bus = Bus.MOD)
public class ToolsDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new ToolsLangProvider(gen));
        }
        if (event.includeServer()) {
            //Server side data generators
            gen.addProvider(new ToolsRecipeProvider(gen));
        }
    }
}