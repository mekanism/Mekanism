package mekanism.additions.common;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.client.AdditionsBlockStateProvider;
import mekanism.additions.client.AdditionsItemModelProvider;
import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.client.AdditionsSpriteSourceProvider;
import mekanism.additions.common.loot.AdditionsLootProvider;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = MekanismAdditions.MODID, bus = Bus.MOD)
public class AdditionsDataGenerator {

    private AdditionsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismAdditions.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        AdditionsDatapackRegistryProvider drProvider = new AdditionsDatapackRegistryProvider(output, event.getLookupProvider());
        CompletableFuture<HolderLookup.Provider> lookupProvider = drProvider.getRegistryProvider();
        gen.addProvider(true, new BasePackMetadataGenerator(output, AdditionsLang.PACK_DESCRIPTION));
        //Client side data generators
        MekanismDataGenerator.addProvider(gen, event.includeClient(), AdditionsLangProvider::new);
        gen.addProvider(event.includeClient(), new AdditionsSoundProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new AdditionsSpriteSourceProvider(output, existingFileHelper, lookupProvider));
        gen.addProvider(event.includeClient(), new AdditionsItemModelProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new AdditionsBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        gen.addProvider(event.includeServer(), new AdditionsTagProvider(output, lookupProvider, existingFileHelper));
        MekanismDataGenerator.addProvider(gen, event.includeServer(), AdditionsLootProvider::new);
        gen.addProvider(event.includeServer(), drProvider);
        gen.addProvider(event.includeServer(), new AdditionsRecipeProvider(output, existingFileHelper, lookupProvider));
        gen.addProvider(event.includeServer(), new AdditionsAdvancementProvider(output, existingFileHelper));
    }
}