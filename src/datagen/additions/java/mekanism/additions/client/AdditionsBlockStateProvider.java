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
    	//Transparent Plastic Blocks
    	coloredBlocks("transparent", AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_BLOCK,
    	      AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_BLOCK,
    	      AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_BLOCK,
    	      AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_BLOCK, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_BLOCK);
        //Plastic Slabs
        coloredSlabs(AdditionsBlocks.BLACK_PLASTIC_SLAB, AdditionsBlocks.RED_PLASTIC_SLAB, AdditionsBlocks.GREEN_PLASTIC_SLAB, AdditionsBlocks.BROWN_PLASTIC_SLAB,
              AdditionsBlocks.BLUE_PLASTIC_SLAB, AdditionsBlocks.PURPLE_PLASTIC_SLAB, AdditionsBlocks.CYAN_PLASTIC_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_SLAB,
              AdditionsBlocks.GRAY_PLASTIC_SLAB, AdditionsBlocks.PINK_PLASTIC_SLAB, AdditionsBlocks.LIME_PLASTIC_SLAB, AdditionsBlocks.YELLOW_PLASTIC_SLAB,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_SLAB, AdditionsBlocks.ORANGE_PLASTIC_SLAB, AdditionsBlocks.WHITE_PLASTIC_SLAB);
        //Plastic Stairs
        coloredStairs(AdditionsBlocks.BLACK_PLASTIC_STAIRS, AdditionsBlocks.RED_PLASTIC_STAIRS, AdditionsBlocks.GREEN_PLASTIC_STAIRS, AdditionsBlocks.BROWN_PLASTIC_STAIRS,
              AdditionsBlocks.BLUE_PLASTIC_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_STAIRS, AdditionsBlocks.CYAN_PLASTIC_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_STAIRS,
              AdditionsBlocks.GRAY_PLASTIC_STAIRS, AdditionsBlocks.PINK_PLASTIC_STAIRS, AdditionsBlocks.LIME_PLASTIC_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_STAIRS,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_STAIRS, AdditionsBlocks.WHITE_PLASTIC_STAIRS);
        //Glow Plastic Slabs
        coloredGlowSlabs(AdditionsBlocks.BLACK_PLASTIC_GLOW_SLAB, AdditionsBlocks.RED_PLASTIC_GLOW_SLAB, AdditionsBlocks.GREEN_PLASTIC_GLOW_SLAB, AdditionsBlocks.BROWN_PLASTIC_GLOW_SLAB,
              AdditionsBlocks.BLUE_PLASTIC_GLOW_SLAB, AdditionsBlocks.PURPLE_PLASTIC_GLOW_SLAB, AdditionsBlocks.CYAN_PLASTIC_GLOW_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_SLAB,
              AdditionsBlocks.GRAY_PLASTIC_GLOW_SLAB, AdditionsBlocks.PINK_PLASTIC_GLOW_SLAB, AdditionsBlocks.LIME_PLASTIC_GLOW_SLAB, AdditionsBlocks.YELLOW_PLASTIC_GLOW_SLAB,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_SLAB, AdditionsBlocks.ORANGE_PLASTIC_GLOW_SLAB, AdditionsBlocks.WHITE_PLASTIC_GLOW_SLAB);
        //Glow Plastic Stairs
        coloredGlowStairs(AdditionsBlocks.BLACK_PLASTIC_GLOW_STAIRS, AdditionsBlocks.RED_PLASTIC_GLOW_STAIRS, AdditionsBlocks.GREEN_PLASTIC_GLOW_STAIRS, AdditionsBlocks.BROWN_PLASTIC_GLOW_STAIRS,
              AdditionsBlocks.BLUE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.CYAN_PLASTIC_GLOW_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_GLOW_STAIRS,
              AdditionsBlocks.GRAY_PLASTIC_GLOW_STAIRS, AdditionsBlocks.PINK_PLASTIC_GLOW_STAIRS, AdditionsBlocks.LIME_PLASTIC_GLOW_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_GLOW_STAIRS,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_GLOW_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_GLOW_STAIRS, AdditionsBlocks.WHITE_PLASTIC_GLOW_STAIRS);
        //Transparent Plastic Slabs
        coloredTransparentSlabs(AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_SLAB,
              AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_SLAB,
              AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_SLAB,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_SLAB, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_SLAB);
        //Transparent Plastic Stairs
        coloredTransparentStairs(AdditionsBlocks.BLACK_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.RED_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.GREEN_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.BROWN_PLASTIC_TRANSPARENT_STAIRS,
              AdditionsBlocks.BLUE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.PURPLE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.CYAN_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.LIGHT_GRAY_PLASTIC_TRANSPARENT_STAIRS,
              AdditionsBlocks.GRAY_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.PINK_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.LIME_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.YELLOW_PLASTIC_TRANSPARENT_STAIRS,
              AdditionsBlocks.LIGHT_BLUE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.MAGENTA_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.ORANGE_PLASTIC_TRANSPARENT_STAIRS, AdditionsBlocks.WHITE_PLASTIC_TRANSPARENT_STAIRS);
    }

    private void coloredBlocks(String modelName, IBlockProvider... blocks) {
        ConfiguredModel model = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/" + modelName)));
        for (IBlockProvider block : blocks) {
        	getVariantBuilder(block).partialState().addModels(model);
        }
    }

    private void coloredSlabs(IBlockProvider... slabs) {
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
        ModelFile stairsModel = models().getExistingFile(modLoc("block/plastic/stairs"));
        ModelFile stairsInner = models().getExistingFile(modLoc("block/plastic/stairs_inner"));
        ModelFile stairsOuter = models().getExistingFile(modLoc("block/plastic/stairs_outer"));
        for (IBlockProvider slab : stairs) {
            stairsBlock((StairsBlock) slab.getBlock(), stairsModel, stairsInner, stairsOuter);
        }
    }

    private void coloredGlowSlabs(IBlockProvider... slabs) {
        ConfiguredModel bottomModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/glow_slab")));
        ConfiguredModel topModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/glow_slab_top")));
        ConfiguredModel doubleModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/glow")));
        for (IBlockProvider slab : slabs) {
            getVariantBuilder(slab)
                  .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(bottomModel)
                  .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(topModel)
                  .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(doubleModel);
        }
    }

    private void coloredGlowStairs(IBlockProvider... stairs) {
        ModelFile stairsModel = models().getExistingFile(modLoc("block/plastic/glow_stairs"));
        ModelFile stairsInner = models().getExistingFile(modLoc("block/plastic/glow_stairs_inner"));
        ModelFile stairsOuter = models().getExistingFile(modLoc("block/plastic/glow_stairs_outer"));
        for (IBlockProvider slab : stairs) {
            stairsBlock((StairsBlock) slab.getBlock(), stairsModel, stairsInner, stairsOuter);
        }
    }

    private void coloredTransparentSlabs(IBlockProvider... slabs) {
        ConfiguredModel bottomModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/transparent_slab")));
        ConfiguredModel topModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/transparent_slab_top")));
        ConfiguredModel doubleModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/transparent")));
        for (IBlockProvider slab : slabs) {
            getVariantBuilder(slab)
                  .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(bottomModel)
                  .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(topModel)
                  .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(doubleModel);
        }
    }

    private void coloredTransparentStairs(IBlockProvider... stairs) {
        ModelFile stairsModel = models().getExistingFile(modLoc("block/plastic/transparent_stairs"));
        ModelFile stairsInner = models().getExistingFile(modLoc("block/plastic/transparent_stairs_inner"));
        ModelFile stairsOuter = models().getExistingFile(modLoc("block/plastic/transparent_stairs_outer"));
        for (IBlockProvider slab : stairs) {
            stairsBlock((StairsBlock) slab.getBlock(), stairsModel, stairsInner, stairsOuter);
        }
    }
}