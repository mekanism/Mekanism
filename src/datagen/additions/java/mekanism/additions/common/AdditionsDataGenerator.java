package mekanism.additions.common;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.client.AdditionsBlockStateProvider;
import mekanism.additions.client.AdditionsItemModelProvider;
import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.client.AdditionsSpriteSourceProvider;
import mekanism.additions.client.integration.emi.AdditionsEmiDefaults;
import mekanism.additions.client.recipe_viewer.aliases.AdditionsAliasMapping;
import mekanism.additions.common.loot.AdditionsLootProvider;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import mekanism.common.BasePackMetadataGenerator;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismAdditions.MODID)
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
        gen.addProvider(event.includeClient(), new AdditionsLangProvider(output));
        gen.addProvider(event.includeClient(), new AdditionsSoundProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new AdditionsSpriteSourceProvider(output, existingFileHelper, lookupProvider));
        gen.addProvider(event.includeClient(), new AdditionsItemModelProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new AdditionsBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        gen.addProvider(event.includeServer(), new AdditionsTagProvider(output, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new AdditionsLootProvider(output, lookupProvider));
        gen.addProvider(event.includeServer(), drProvider);
        gen.addProvider(event.includeServer(), new AdditionsDataMapsProvider(output, lookupProvider));
        gen.addProvider(event.includeServer(), new AdditionsRecipeProvider(output, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new AdditionsAdvancementProvider(output, lookupProvider, existingFileHelper));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, lookupProvider, MekanismAdditions.MODID, AdditionsAliasMapping::new,
              () -> AdditionsEmiDefaults::new);
    }
}