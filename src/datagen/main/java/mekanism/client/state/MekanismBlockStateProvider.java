package mekanism.client.state;

import java.util.Map;
import mekanism.client.model.MekanismBlockModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockOre;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.resource.IResource;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
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

        BlockModelBuilder barrelModel = models().cubeBottomTop(MekanismBlocks.PERSONAL_BARREL.getName(),
              Mekanism.rl("block/personal_barrel/side"),
              Mekanism.rl("block/personal_barrel/bottom"),
              Mekanism.rl("block/personal_barrel/top")
        );
        BlockModelBuilder openBarrel = models().getBuilder(MekanismBlocks.PERSONAL_BARREL.getName() + "_open").parent(barrelModel)
              .texture("top", Mekanism.rl("block/personal_barrel/top_open"));
        directionalBlock(MekanismBlocks.PERSONAL_BARREL.getBlock(), state -> state.getValue(BlockStateProperties.OPEN) ? openBarrel : barrelModel);
        simpleBlockItem(MekanismBlocks.PERSONAL_BARREL, barrelModel);

        BlockModelBuilder stabilizerModel = models().cubeBottomTop(MekanismBlocks.DIMENSIONAL_STABILIZER.getName(),
              Mekanism.rl("block/dimensional_stabilizer/side"),
              Mekanism.rl("block/dimensional_stabilizer/bottom"),
              Mekanism.rl("block/dimensional_stabilizer/top")
        );
        BlockModelBuilder activeStabilizer = models().getBuilder(MekanismBlocks.DIMENSIONAL_STABILIZER.getName() + "_active").parent(stabilizerModel)
              .texture("top", Mekanism.rl("block/dimensional_stabilizer/top_active"))
              .texture("side", Mekanism.rl("block/dimensional_stabilizer/side_active"));
        simpleBlockItem(MekanismBlocks.DIMENSIONAL_STABILIZER, stabilizerModel);
        getVariantBuilder(MekanismBlocks.DIMENSIONAL_STABILIZER.getBlock())
              .forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(Attribute.isActive(state) ? activeStabilizer : stabilizerModel)});
    }

    private void addOreBlock(ResourceLocation basicCube, BlockRegistryObject<BlockOre, ?> oreBlock, String path) {
        String name = oreBlock.getName();
        ModelFile file = models().withExistingParent(path, basicCube)
              .texture("all", modLoc("block/" + name));
        simpleBlock(oreBlock.getBlock(), file);
        simpleBlockItem(oreBlock, file);
    }
}