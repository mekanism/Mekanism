package mekanism.tools.common;

import mekanism.tools.client.ToolsItemModelProvider;
import mekanism.tools.client.ToolsLangProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(modid = MekanismTools.MODID, bus = Bus.MOD)
public class ToolsDataGenerator {

    private ToolsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeClient()) {
            //Client side data generators
            gen.addProvider(new ToolsLangProvider(gen));
            gen.addProvider(new ToolsItemModelProvider(gen, existingFileHelper));
        }
        if (event.includeServer()) {
            //Server side data generators
            gen.addProvider(new ToolsTagProvider(gen, existingFileHelper));
            gen.addProvider(new ToolsRecipeProvider(gen));
        }
    }
}