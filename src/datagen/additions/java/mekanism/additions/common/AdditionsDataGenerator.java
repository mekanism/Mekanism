package mekanism.additions.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsModelProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.client.AdditionsSpriteSourceProvider;
import mekanism.additions.client.integration.emi_no_dep.AdditionsEmiDefaults;
import mekanism.additions.client.recipe_viewer.aliases.AdditionsAliasMapping;
import mekanism.additions.common.loot.AdditionsLootProvider;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
import mekanism.common.recipe.MekRecipeRunner;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
        AdditionsDatapackRegistryProvider drProvider = new AdditionsDatapackRegistryProvider(output, MekanismDataGenerator.getLookupProvider());
        CompletableFuture<HolderLookup.Provider> lookupProvider = drProvider.getRegistryProvider();
        ResourceManager clientResources = event.getResourceManager(PackType.CLIENT_RESOURCES);
        //Client side data generators
        gen.addProvider(true, new AdditionsLangProvider(output));
        gen.addProvider(true, new AdditionsSoundProvider(output));
        gen.addProvider(true, new AdditionsSpriteSourceProvider(output, lookupProvider));
        gen.addProvider(true, new AdditionsModelProvider(output, clientResources));
        //Server side data generators
        gen.addProvider(true, new AdditionsTagProvider(output, lookupProvider));
        gen.addProvider(true, new AdditionsLootProvider(output, lookupProvider));
        gen.addProvider(true, drProvider);
        gen.addProvider(true, new AdditionsDataMapsProvider(output, lookupProvider));
        gen.addProvider(true, new MekRecipeRunner(output, lookupProvider, AdditionsRecipeProvider::new, MekanismAdditions.MODID));
        gen.addProvider(true, new AdvancementProvider(output, lookupProvider, List.of(new AdditionsAdvancementProvider())));
        gen.addProvider(true, new AdditionsEmiDefaults(output, event.getResourceManager(PackType.SERVER_DATA), lookupProvider));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, lookupProvider, MekanismAdditions.MODID, AdditionsAliasMapping::new);
    }
}