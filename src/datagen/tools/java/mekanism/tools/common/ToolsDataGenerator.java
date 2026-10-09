package mekanism.tools.common;

import mekanism.common.PersistingDisabledProvidersProvider;
import mekanism.tools.client.ToolsEquipmentAssetProvider;
import mekanism.tools.client.ToolsLangProvider;
import mekanism.tools.client.ToolsModelProvider;
import mekanism.tools.client.ToolsSplashProvider;
import mekanism.tools.client.ToolsSpriteSourceProvider;
import mekanism.tools.client.integration.emi.ToolsEmiDefaults;
import mekanism.tools.client.recipe_viewer.aliases.ToolsAliasMapping;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismTools.MODID)
public class ToolsDataGenerator {

    private ToolsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        //Client side data generators
        event.createProvider(ToolsLangProvider::new);
        event.createProvider(ToolsSpriteSourceProvider::new);
        event.createProvider(output -> new ToolsModelProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        event.createProvider(ToolsEquipmentAssetProvider::new);
        event.createProvider(ToolsSplashProvider::new);
        //Server side data generators
        event.createProvider(ToolsTagProvider::new);
        event.createProvider(ToolsEmiDefaults::new);
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, ToolsAliasMapping::new);
    }
}