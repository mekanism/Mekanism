package mekanism.tools.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
import mekanism.tools.client.ToolsEquipmentAssetProvider;
import mekanism.tools.client.ToolsLangProvider;
import mekanism.tools.client.ToolsModelProvider;
import mekanism.tools.client.ToolsSpriteSourceProvider;
import mekanism.tools.client.integration.emi.ToolsEmiDefaults;
import mekanism.tools.client.recipe_viewer.aliases.ToolsAliasMapping;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismTools.MODID)
public class ToolsDataGenerator {

    private ToolsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismTools.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        DatapackBuiltinEntriesProvider worldRegistryProvider = ToolsRegistryProvider.forWorldLayer(output, MekanismDataGenerator.getWorldLookupProvider());
        CompletableFuture<HolderLookup.Provider> worldLookupProvider = worldRegistryProvider.getRegistryProvider();
        DatapackBuiltinEntriesProvider reloadableRegistryProvider = ToolsRegistryProvider.forReloadableLayer(output, worldLookupProvider, MekanismDataGenerator.getReloadableLookupProvider());
        CompletableFuture<HolderLookup.Provider> reloadableLookupProvider = reloadableRegistryProvider.getRegistryProvider();
        ResourceManager clientResources = event.getResourceManager(PackType.CLIENT_RESOURCES);
        //Client side data generators
        gen.addProvider(true, new ToolsLangProvider(output));
        gen.addProvider(true, new ToolsSpriteSourceProvider(output, worldLookupProvider));
        gen.addProvider(true, new ToolsModelProvider(output, clientResources));
        gen.addProvider(true, new ToolsEquipmentAssetProvider(output));
        //Server side data generators
        gen.addProvider(true, new ToolsTagProvider(output, reloadableLookupProvider));
        gen.addProvider(true, worldRegistryProvider);
        gen.addProvider(true, reloadableRegistryProvider);
        gen.addProvider(true, new ToolsEmiDefaults(output, event.getResourceManager(PackType.SERVER_DATA), reloadableLookupProvider));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, reloadableLookupProvider, MekanismTools.MODID, ToolsAliasMapping::new);
    }
}