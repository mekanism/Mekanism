package mekanism.tools.common;

import mekanism.common.MekanismDataGenerator;
import mekanism.tools.client.ToolsItemModelProvider;
import mekanism.tools.client.ToolsLangProvider;
import mekanism.tools.common.recipe.ToolsRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = MekanismTools.MODID, bus = Bus.MOD)
public class ToolsDataGenerator {

    private ToolsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismTools.MODID);
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        //Client side data generators
        gen.addProvider(event.includeClient(), new ToolsLangProvider(gen));
        gen.addProvider(event.includeClient(), new ToolsItemModelProvider(gen, existingFileHelper));
        //Server side data generators
        gen.addProvider(event.includeServer(), new ToolsTagProvider(gen, existingFileHelper));
        gen.addProvider(event.includeServer(), new ToolsRecipeProvider(gen, existingFileHelper));
        gen .addProvider(event.includeServer(), new ToolsAdvancementProvider(gen, existingFileHelper));
    }
}