package mekanism.tools.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
import mekanism.common.recipe.MekRecipeRunner;
import mekanism.tools.client.ToolsModelProvider;
import mekanism.tools.client.ToolsLangProvider;
import mekanism.tools.client.ToolsSpriteSourceProvider;
import mekanism.tools.client.integration.emi.ToolsEmiDefaults;
import mekanism.tools.client.recipe_viewer.aliases.ToolsAliasMapping;
import mekanism.tools.common.recipe.ToolsRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ResourceManager clientResources = event.getResourceManager(PackType.CLIENT_RESOURCES);
        //Client side data generators
        gen.addProvider(true, new ToolsLangProvider(output));
        gen.addProvider(true, new ToolsSpriteSourceProvider(output, lookupProvider));
        gen.addProvider(true, new ToolsModelProvider(output, clientResources));
        //Server side data generators
        gen.addProvider(true, new ToolsTagProvider(output, lookupProvider));
        gen.addProvider(true, new MekRecipeRunner(output, lookupProvider, ToolsRecipeProvider::new, MekanismTools.MODID));
        gen.addProvider(true, new AdvancementProvider(output, lookupProvider, List.of(new ToolsAdvancementProvider())));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, lookupProvider, MekanismTools.MODID, ToolsAliasMapping::new, () -> ToolsEmiDefaults::new);
    }
}