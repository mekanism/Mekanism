package mekanism.tools.common;

import mekanism.common.MekanismDataGenerator;
import mekanism.common.registries.BaseRegistryProvider;
import mekanism.tools.common.recipe.ToolsRecipeProvider;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = MekanismTools.MODID)
public class ToolsRegistryProvider extends BaseRegistryProvider {

    private ToolsRegistryProvider() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGatherRegistries(GatherDataRegistryEntriesEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismTools.MODID);
        event.advancement(ToolsAdvancementProvider::new)
              .recipe(ToolsRecipeProvider::new)
        ;
    }
}