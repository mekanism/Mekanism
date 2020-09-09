package mekanism.client.state;

import java.util.Map;
import mekanism.client.model.MekanismBlockModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.resource.OreType;
import mekanism.common.resource.PrimaryResource;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MekanismBlockStateProvider extends BaseBlockStateProvider<MekanismBlockModelProvider> {

    public MekanismBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Mekanism.MODID, existingFileHelper, MekanismBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidBlockStates(MekanismFluids.FLUIDS.getAllFluids());

        ResourceLocation basicCube = modLoc("block/basic_cube");

        // blocks
        for (Map.Entry<PrimaryResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            ResourceLocation texture = modLoc("block/block_" + entry.getKey().getName());
            ModelFile file;
            if (models().textureExists(texture)) {
                //If we have an override we can just use a basic cube that has no color tints in it
                file = models().withExistingParent("block/storage/" + entry.getKey().getName(), basicCube)
                      .texture("all", texture);
            } else {
                //If the texture does not exist fallback to the default texture and use a colorable base model
                file = models().withExistingParent("block/storage/" + entry.getKey().getName(), modLoc("block/colored_cube"))
                      .texture("all", modLoc("block/resource_block"));
            }
            simpleBlock(entry.getValue().getBlock(), file);
        }
        for (Map.Entry<OreType, BlockRegistryObject<?, ?>> entry : MekanismBlocks.ORES.entrySet()) {
            ModelFile file = models().withExistingParent("block/ore/" + entry.getKey().getResource().getRegistrySuffix(), basicCube)
                  .texture("all", modLoc("block/" + entry.getValue().getName()));
            simpleBlock(entry.getValue().getBlock(), file);
        }
        // block items
        for (Map.Entry<PrimaryResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            models().withExistingParent("item/block_" + entry.getKey().getName(), modLoc("block/storage/" + entry.getKey().getName()));
        }
        for (Map.Entry<OreType, BlockRegistryObject<?, ?>> entry : MekanismBlocks.ORES.entrySet()) {
            models().withExistingParent("item/" + entry.getKey().getResource().getRegistrySuffix() + "_ore", modLoc("block/ore/" + entry.getKey().getResource().getRegistrySuffix()));
        }
    }
}