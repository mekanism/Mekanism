package mekanism.additions.common;

import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsModelProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.client.AdditionsSplashProvider;
import mekanism.additions.client.AdditionsSpriteSourceProvider;
import mekanism.additions.client.integration.emi.AdditionsEmiDefaults;
import mekanism.additions.client.recipe_viewer.aliases.AdditionsAliasMapping;
import mekanism.common.PersistingDisabledProvidersProvider;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismAdditions.MODID)
public class AdditionsDataGenerator {

    private AdditionsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        //Client side data generators
        event.createProvider(AdditionsLangProvider::new);
        event.createProvider(AdditionsSoundProvider::new);
        event.createProvider(AdditionsSpriteSourceProvider::new);
        event.createProvider(output -> new AdditionsModelProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        event.createProvider(AdditionsSplashProvider::new);
        //Server side data generators
        event.createProvider(AdditionsTagProvider::new);
        event.createProvider(AdditionsDataMapsProvider::new);
        event.createProvider(AdditionsEmiDefaults::new);
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, AdditionsAliasMapping::new);
    }
}