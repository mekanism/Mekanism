package mekanism.generators.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
import mekanism.generators.client.GeneratorsLangProvider;
import mekanism.generators.client.GeneratorsModelProvider;
import mekanism.generators.client.GeneratorsSoundProvider;
import mekanism.generators.client.integration.emi.GeneratorsEmiDefaults;
import mekanism.generators.client.recipe_viewer.alias.GeneratorsAliasMapping;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismGenerators.MODID)
public class GeneratorsDataGenerator {

    private GeneratorsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismGenerators.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        DatapackBuiltinEntriesProvider worldRegistryProvider = GeneratorsRegistryProvider.forWorldLayer(output, MekanismDataGenerator.getWorldLookupProvider());
        CompletableFuture<HolderLookup.Provider> worldLookupProvider = worldRegistryProvider.getRegistryProvider();
        DatapackBuiltinEntriesProvider reloadableRegistryProvider = GeneratorsRegistryProvider.forReloadableLayer(output, worldLookupProvider, MekanismDataGenerator.getReloadableLookupProvider());
        CompletableFuture<HolderLookup.Provider> reloadableLookupProvider = reloadableRegistryProvider.getRegistryProvider();
        ResourceManager clientResources = event.getResourceManager(PackType.CLIENT_RESOURCES);
        //Client side data generators
        gen.addProvider(true, new GeneratorsLangProvider(output));
        gen.addProvider(true, new GeneratorsSoundProvider(output));
        gen.addProvider(true, new GeneratorsModelProvider(output, clientResources));
        //Server side data generators
        gen.addProvider(true, new GeneratorsTagProvider(output, reloadableLookupProvider));
        gen.addProvider(true, worldRegistryProvider);
        gen.addProvider(true, reloadableRegistryProvider);
        gen.addProvider(true, new GeneratorsDataMapsProvider(output, reloadableLookupProvider));
        gen.addProvider(true, new GeneratorsEmiDefaults(output, event.getResourceManager(PackType.SERVER_DATA), reloadableLookupProvider));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, reloadableLookupProvider, MekanismGenerators.MODID, GeneratorsAliasMapping::new);
    }
}