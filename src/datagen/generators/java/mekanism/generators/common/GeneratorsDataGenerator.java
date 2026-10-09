package mekanism.generators.common;

import mekanism.common.PersistingDisabledProvidersProvider;
import mekanism.generators.client.GeneratorsLangProvider;
import mekanism.generators.client.GeneratorsModelProvider;
import mekanism.generators.client.GeneratorsSoundProvider;
import mekanism.generators.client.GeneratorsSplashProvider;
import mekanism.generators.client.integration.emi.GeneratorsEmiDefaults;
import mekanism.generators.client.recipe_viewer.alias.GeneratorsAliasMapping;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismGenerators.MODID)
public class GeneratorsDataGenerator {

    private GeneratorsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        //Client side data generators
        event.createProvider(GeneratorsLangProvider::new);
        event.createProvider(GeneratorsSoundProvider::new);
        event.createProvider(output -> new GeneratorsModelProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        event.createProvider(GeneratorsSplashProvider::new);
        //Server side data generators
        event.createProvider(GeneratorsTagProvider::new);
        event.createProvider(GeneratorsDataMapsProvider::new);
        event.createProvider(GeneratorsEmiDefaults::new);
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, GeneratorsAliasMapping::new);
    }
}