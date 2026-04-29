package mekanism.generators.client;

import mekanism.client.model.BaseModelProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.resources.ResourceManager;

public class GeneratorsItemModelProvider extends BaseModelProvider {

    public GeneratorsItemModelProvider(PackOutput output, ResourceManager clientResources) {
        super(output, MekanismGenerators.MODID, clientResources);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        registerBuckets(GeneratorsFluids.FLUIDS, itemModels);
        registerModules(GeneratorsItems.ITEMS, itemModels);
        registerGenerated(itemModels, GeneratorsItems.HOHLRAUM, GeneratorsItems.SOLAR_PANEL, GeneratorsItems.TURBINE_BLADE);
    }
}