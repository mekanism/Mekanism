package mekanism.client.state;

import java.util.Map;
import mekanism.client.model.MekanismBlockModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.resource.IResource;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MekanismBlockStateProvider extends BaseBlockStateProvider<MekanismBlockModelProvider> {

    public MekanismBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Mekanism.MODID, existingFileHelper, MekanismBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidBlockStates(MekanismFluids.FLUIDS);

        for (Map.Entry<IResource, ? extends Holder<Block>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
            String registrySuffix = entry.getKey().getRegistrySuffix();
            ResourceLocation texture = modLoc("block/block_" + registrySuffix);
            ModelFile file;
            if (models().textureExists(texture)) {
                //If we have an override we can just use a basic cube that has no color tints in it
                file = models().cubeAll("block/storage/" + registrySuffix, texture);
            } else {
                //If the texture does not exist fallback to the default texture and use a colorable base model
                file = models().withExistingParent("block/storage/" + registrySuffix, modLoc("block/colored_cube"))
                      .texture("all", modLoc("block/resource_block"));
            }
            simpleBlock(entry.getValue(), file);

            models().withExistingParent("item/block_" + registrySuffix, modLoc("block/storage/" + registrySuffix));
        }
        for (Map.Entry<OreType, OreBlockType> entry : MekanismBlocks.ORES.entrySet()) {
            String registrySuffix = entry.getKey().getResource().getRegistrySuffix();
            OreBlockType oreBlockType = entry.getValue();
            addOreBlock(oreBlockType.stone(), "block/ore/" + registrySuffix);
            addOreBlock(oreBlockType.deepslate(), "block/deepslate_ore/" + registrySuffix);
        }

        BlockModelBuilder barrelModel = models().cubeBottomTop(MekanismBlocks.PERSONAL_BARREL.getName(),
              Mekanism.rl("block/personal_barrel/side"),
              Mekanism.rl("block/personal_barrel/bottom"),
              Mekanism.rl("block/personal_barrel/top")
        );
        BlockModelBuilder openBarrel = models().getBuilder(MekanismBlocks.PERSONAL_BARREL.getName() + "_open").parent(barrelModel)
              .texture("top", Mekanism.rl("block/personal_barrel/top_open"));
        directionalBlock(MekanismBlocks.PERSONAL_BARREL, state -> state.getValue(BlockStateProperties.OPEN) ? openBarrel : barrelModel);
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
        getVariantBuilder(MekanismBlocks.DIMENSIONAL_STABILIZER)
              .forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(Attribute.isActive(state) ? activeStabilizer : stabilizerModel)});
    }

    private void addOreBlock(Holder<Block> oreBlock, String path) {
        ModelFile file = models().cubeAll(path, modLoc("block/" + getPath(oreBlock)));
        simpleBlock(oreBlock, file);
        simpleBlockItem(oreBlock, file);
    }
}