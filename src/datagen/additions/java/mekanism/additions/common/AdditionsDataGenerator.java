package mekanism.additions.common;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsModelProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.client.AdditionsSplashProvider;
import mekanism.additions.client.AdditionsSpriteSourceProvider;
import mekanism.additions.client.integration.emi.AdditionsEmiDefaults;
import mekanism.additions.client.recipe_viewer.aliases.AdditionsAliasMapping;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismAdditions.MODID)
public class AdditionsDataGenerator {

    private AdditionsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismAdditions.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        DatapackBuiltinEntriesProvider worldRegistryProvider = AdditionsRegistryProvider.forWorldLayer(output, MekanismDataGenerator.getWorldLookupProvider());
        CompletableFuture<HolderLookup.Provider> worldLookupProvider = worldRegistryProvider.getRegistryProvider();
        DatapackBuiltinEntriesProvider reloadableRegistryProvider = AdditionsRegistryProvider.forReloadableLayer(output, worldLookupProvider, MekanismDataGenerator.getReloadableLookupProvider());
        CompletableFuture<HolderLookup.Provider> reloadableLookupProvider = reloadableRegistryProvider.getRegistryProvider();
        ResourceManager clientResources = event.getResourceManager(PackType.CLIENT_RESOURCES);
        //Client side data generators
        gen.addProvider(true, new AdditionsLangProvider(output));
        gen.addProvider(true, new AdditionsSoundProvider(output));
        gen.addProvider(true, new AdditionsSpriteSourceProvider(output, worldLookupProvider));
        gen.addProvider(true, new AdditionsModelProvider(output, clientResources));
        gen.addProvider(true, new AdditionsSplashProvider(output));
        //Server side data generators
        gen.addProvider(true, new AdditionsTagProvider(output, reloadableLookupProvider));
        gen.addProvider(true, worldRegistryProvider);
        gen.addProvider(true, reloadableRegistryProvider);
        gen.addProvider(true, new AdditionsDataMapsProvider(output, reloadableLookupProvider));
        gen.addProvider(true, new AdditionsEmiDefaults(output, reloadableLookupProvider));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, reloadableLookupProvider, MekanismAdditions.MODID, AdditionsAliasMapping::new);
    }
}