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
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;

public class MekanismBlockStateProvider extends BaseBlockStateProvider<MekanismBlockModelProvider> {

    public MekanismBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Mekanism.MODID, existingFileHelper, MekanismBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidBlockStates(MekanismFluids.FLUIDS.getAllFluids());

        // blocks
        for (Map.Entry<PrimaryResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            String texture = entry.getKey().hasTextureOverride() ? "block/block_" + entry.getKey().getName() : "block/resource_block";
            ModelFile file = models().withExistingParent("block/storage/" + entry.getKey().getName(), modLoc("block/colored_cube"))
                  .texture("all", modLoc(texture));
            simpleBlock(entry.getValue().getBlock(), file);
        }
        for (Map.Entry<OreType, BlockRegistryObject<?, ?>> entry : MekanismBlocks.ORES.entrySet()) {
            ModelFile file = models().withExistingParent("block/ore/" + entry.getKey().getResource().getRegistrySuffix(), modLoc("block/basic_cube"))
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