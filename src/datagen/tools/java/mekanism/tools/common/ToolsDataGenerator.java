package mekanism.tools.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.tools.client.ToolsItemModelProvider;
import mekanism.tools.client.ToolsLangProvider;
import mekanism.tools.common.recipe.ToolsRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
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
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        addProvider(gen, true, output -> new BasePackMetadataGenerator(output, ToolsLang.PACK_DESCRIPTION));
        //Client side data generators
        addProvider(gen, event.includeClient(), ToolsLangProvider::new);
        addProvider(gen, event.includeClient(), output -> new ToolsItemModelProvider(output, existingFileHelper));
        //Server side data generators
        addProvider(gen, event.includeServer(), output -> new ToolsTagProvider(output, lookupProvider, existingFileHelper));
        addProvider(gen, event.includeServer(), output -> new ToolsRecipeProvider(output, existingFileHelper));
        addProvider(gen, event.includeServer(), output -> new ToolsAdvancementProvider(output, existingFileHelper));
    }

    private static <PROVIDER extends DataProvider> void addProvider(DataGenerator gen, boolean run, DataProvider.Factory<PROVIDER> factory) {
        gen.addProvider(run, factory);
    }
}