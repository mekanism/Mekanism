package mekanism.client.state;

import java.util.Map;
import mekanism.client.model.MekanismBlockModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockOre;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.resource.IResource;
import mekanism.common.resource.ore.OreType;
import mekanism.common.resource.ore.OreBlockType;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
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

        for (Map.Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            String registrySuffix = entry.getKey().getRegistrySuffix();
            ResourceLocation texture = modLoc("block/block_" + registrySuffix);
            ModelFile file;
            if (models().textureExists(texture)) {
                //If we have an override we can just use a basic cube that has no color tints in it
                file = models().withExistingParent("block/storage/" + registrySuffix, basicCube)
                      .texture("all", texture);
            } else {
                //If the texture does not exist fallback to the default texture and use a colorable base model
                file = models().withExistingParent("block/storage/" + registrySuffix, modLoc("block/colored_cube"))
                      .texture("all", modLoc("block/resource_block"));
            }
            simpleBlock(entry.getValue().getBlock(), file);

            models().withExistingParent("item/block_" + registrySuffix, modLoc("block/storage/" + registrySuffix));
        }
        for (Map.Entry<OreType, OreBlockType> entry : MekanismBlocks.ORES.entrySet()) {
            String registrySuffix = entry.getKey().getResource().getRegistrySuffix();
            OreBlockType oreBlockType = entry.getValue();
            addOreBlock(basicCube, oreBlockType.stone(), "block/ore/" + registrySuffix);
            addOreBlock(basicCube, oreBlockType.deepslate(), "block/deepslate_ore/" + registrySuffix);
        }
    }

    private void addOreBlock(ResourceLocation basicCube, BlockRegistryObject<BlockOre, ?> oreBlock, String path) {
        String name = oreBlock.getName();
        ModelFile file = models().withExistingParent(path, basicCube)
              .texture("all", modLoc("block/" + name));
        simpleBlock(oreBlock.getBlock(), file);
        models().withExistingParent("item/" + name, modLoc(path));
    }
}