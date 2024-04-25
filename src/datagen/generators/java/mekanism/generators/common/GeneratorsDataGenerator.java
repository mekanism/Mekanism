package mekanism.generators.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.generators.client.GeneratorsBlockStateProvider;
import mekanism.generators.client.GeneratorsItemModelProvider;
import mekanism.generators.client.GeneratorsLangProvider;
import mekanism.generators.client.GeneratorsSoundProvider;
import mekanism.generators.client.GeneratorsSpriteSourceProvider;
import mekanism.generators.common.loot.GeneratorsLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismGenerators.MODID, bus = EventBusSubscriber.Bus.MOD)
public class GeneratorsDataGenerator {

    private GeneratorsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismGenerators.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        gen.addProvider(true, new BasePackMetadataGenerator(output, GeneratorsLang.PACK_DESCRIPTION));
        //Client side data generators
        gen.addProvider(event.includeClient(), new GeneratorsLangProvider(output));
        gen.addProvider(event.includeClient(), new GeneratorsSoundProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new GeneratorsSpriteSourceProvider(output, existingFileHelper, lookupProvider));
        gen.addProvider(event.includeClient(), new GeneratorsItemModelProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new GeneratorsBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        gen.addProvider(event.includeServer(), new GeneratorsTagProvider(output, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new GeneratorsLootProvider(output, lookupProvider));
        gen.addProvider(event.includeServer(), new GeneratorsRecipeProvider(output, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new GeneratorsAdvancementProvider(output, lookupProvider, existingFileHelper));
    }
}