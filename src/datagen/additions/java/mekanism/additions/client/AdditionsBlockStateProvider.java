package mekanism.additions.client;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.state.BaseBlockStateProvider;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.SlabType;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;

public class AdditionsBlockStateProvider extends BaseBlockStateProvider<AdditionsBlockModelProvider> {

    public AdditionsBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismAdditions.MODID, existingFileHelper, AdditionsBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        //Slabs
        coloredSlabs(AdditionsBlocks.BLACK_PLASTIC_SLAB, AdditionsBlocks.RED_PLASTIC_SLAB, AdditionsBlocks.GREEN_PLASTIC_SLAB, AdditionsBlocks.BROWN_PLASTIC_SLAB,
              AdditionsBlocks.BLUE_PLASTIC_SLAB, AdditionsBlocks.PURPLE_PLASTIC_SLAB, AdditionsBlocks.CYAN_PLASTIC_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_SLAB,
              AdditionsBlocks.GRAY_PLASTIC_SLAB, AdditionsBlocks.PINK_PLASTIC_SLAB, AdditionsBlocks.LIME_PLASTIC_SLAB, AdditionsBlocks.YELLOW_PLASTIC_SLAB,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_SLAB, AdditionsBlocks.ORANGE_PLASTIC_SLAB, AdditionsBlocks.WHITE_PLASTIC_SLAB);
        //Stairs
        coloredStairs(AdditionsBlocks.BLACK_PLASTIC_STAIRS, AdditionsBlocks.RED_PLASTIC_STAIRS, AdditionsBlocks.GREEN_PLASTIC_STAIRS, AdditionsBlocks.BROWN_PLASTIC_STAIRS,
              AdditionsBlocks.BLUE_PLASTIC_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_STAIRS, AdditionsBlocks.CYAN_PLASTIC_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_STAIRS,
              AdditionsBlocks.GRAY_PLASTIC_STAIRS, AdditionsBlocks.PINK_PLASTIC_STAIRS, AdditionsBlocks.LIME_PLASTIC_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_STAIRS,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_STAIRS, AdditionsBlocks.WHITE_PLASTIC_STAIRS);
    }

    private void coloredSlabs(IBlockProvider... slabs) {
        //TODO: Generate these models and the colored variants
        ConfiguredModel bottomModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/slab")));
        ConfiguredModel topModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/slab_top")));
        ConfiguredModel doubleModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/block")));
        for (IBlockProvider slab : slabs) {
            getVariantBuilder(slab)
                  .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(bottomModel)
                  .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(topModel)
                  .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(doubleModel);
        }
    }

    private void coloredStairs(IBlockProvider... stairs) {
        //TODO: Generate the colored models
        ModelFile stairsModel = models().sideBottomTop("block/plastic/stairs", modLoc("block/colored/stairs"), modLoc("block/plastic_block"));
        ModelFile stairsInner = models().sideBottomTop("block/plastic/stairs_inner", modLoc("block/colored/stairs_inner"), modLoc("block/plastic_block"));
        ModelFile stairsOuter = models().sideBottomTop("block/plastic/stairs_outer", modLoc("block/colored/stairs_outer"), modLoc("block/plastic_block"));
        for (IBlockProvider slab : stairs) {
            stairsBlock((StairsBlock) slab.getBlock(), stairsModel, stairsInner, stairsOuter);
        }
    }
}