package mekanism.additions.client;

import java.util.Map;
import java.util.function.Function;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.block.BlockGlowPanel;
import mekanism.additions.common.block.plastic.BlockPlasticFenceGate;
import mekanism.additions.common.block.plastic.BlockPlasticStairs;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.client.state.BaseBlockStateProvider;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AdditionsBlockStateProvider extends BaseBlockStateProvider<AdditionsBlockModelProvider> {

    public AdditionsBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismAdditions.MODID, existingFileHelper, AdditionsBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        glowPanels();
        coloredBlocks(AdditionsBlocks.PLASTIC_BLOCKS, "block");
        coloredBlocks(AdditionsBlocks.SLICK_PLASTIC_BLOCKS, "slick");
        coloredBlocks(AdditionsBlocks.PLASTIC_GLOW_BLOCKS, "glow");
        coloredBlocks(AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, "reinforced");
        coloredBlocks(AdditionsBlocks.PLASTIC_ROADS, "road");
        coloredBlocks(AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, "transparent");
        coloredSlabs(AdditionsBlocks.PLASTIC_SLABS, "", "block");
        coloredStairs(AdditionsBlocks.PLASTIC_STAIRS, "");
        coloredFences(AdditionsBlocks.PLASTIC_FENCES, "");
        coloredFenceGates(AdditionsBlocks.PLASTIC_FENCE_GATES, "");
        coloredSlabs(AdditionsBlocks.PLASTIC_GLOW_SLABS, "glow_", "glow");
        coloredStairs(AdditionsBlocks.PLASTIC_GLOW_STAIRS, "glow_");
        coloredSlabs(AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, "transparent_", "transparent");
        coloredStairs(AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS, "transparent_");
    }

    private void glowPanels() {
        ModelFile model = models().getExistingFile(modLoc("block/glow_panel"));
        Function<BlockState, ModelFile> modelFunc = state -> model;
        for (BlockRegistryObject<BlockGlowPanel, ?> blockRO : AdditionsBlocks.GLOW_PANELS.values()) {
            directionalBlock(blockRO, modelFunc, 180, blockRO.value().getFluidLoggedProperty());
        }
    }

    private void coloredBlocks(Map<?, ? extends Holder<Block>> blocks, String modelName) {
        ConfiguredModel model = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/" + modelName)));
        for (Holder<Block> block : blocks.values()) {
            getVariantBuilder(block).partialState().addModels(model);
        }
    }

    private void coloredSlabs(Map<?, ? extends Holder<Block>> slabs, String existingPrefix, String doubleType) {
        ConfiguredModel bottomModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "slab")));
        ConfiguredModel topModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "slab_top")));
        ConfiguredModel doubleModel = new ConfiguredModel(models().getExistingFile(modLoc("block/plastic/" + doubleType)));
        for (Holder<Block> slab : slabs.values()) {
            getVariantBuilder(slab)
                  .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(bottomModel)
                  .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(topModel)
                  .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(doubleModel);
        }
    }

    private void coloredStairs(Map<?, ? extends BlockRegistryObject<? extends BlockPlasticStairs, ?>> stairs, String existingPrefix) {
        ModelFile stairsModel = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "stairs"));
        ModelFile stairsInner = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "stairs_inner"));
        ModelFile stairsOuter = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "stairs_outer"));
        for (BlockRegistryObject<? extends BlockPlasticStairs, ?> stair : stairs.values()) {
            //Copy of BlockStateProvider#stairsBlock, except also ignores our fluid logging extension
            getVariantBuilder(stair).forAllStatesExcept(state -> {
                Direction facing = state.getValue(StairBlock.FACING);
                Half half = state.getValue(StairBlock.HALF);
                StairsShape shape = state.getValue(StairBlock.SHAPE);
                int yRot = (int) facing.getClockWise().toYRot(); // Stairs model is rotated 90 degrees clockwise for some reason
                if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) {
                    yRot += 270; // Left facing stairs are rotated 90 degrees clockwise
                }
                if (shape != StairsShape.STRAIGHT && half == Half.TOP) {
                    yRot += 90; // Top stairs are rotated 90 degrees clockwise
                }
                yRot %= 360;
                boolean uvlock = yRot != 0 || half == Half.TOP; // Don't set uvlock for states that have no rotation
                return ConfiguredModel.builder()
                      .modelFile(shape == StairsShape.STRAIGHT ? stairsModel : shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? stairsInner : stairsOuter)
                      .rotationX(half == Half.BOTTOM ? 0 : 180)
                      .rotationY(yRot)
                      .uvLock(uvlock)
                      .build();
            }, StairBlock.WATERLOGGED, stair.value().getFluidLoggedProperty());
        }
    }

    private void coloredFences(Map<?, ? extends Holder<Block>> fences, String existingPrefix) {
        ModelFile post = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "fence_post"));
        ModelFile side = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "fence_side"));
        for (Holder<Block> fence : fences.values()) {
            fourWayMultipart(getMultipartBuilder(fence.value()).part().modelFile(post).addModel().end(), side);
        }
    }

    private void coloredFenceGates(Map<?, ? extends BlockRegistryObject<? extends BlockPlasticFenceGate, ?>> fenceGates, String existingPrefix) {
        ModelFile gate = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "fence_gate"));
        ModelFile gateOpen = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "fence_gate_open"));
        ModelFile gateWall = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "fence_gate_wall"));
        ModelFile gateWallOpen = models().getExistingFile(modLoc("block/plastic/" + existingPrefix + "fence_gate_wall_open"));
        for (BlockRegistryObject<? extends BlockPlasticFenceGate, ?> fenceGate : fenceGates.values()) {
            getVariantBuilder(fenceGate).forAllStatesExcept(state -> {
                ModelFile model = gate;
                if (state.getValue(FenceGateBlock.IN_WALL)) {
                    model = gateWall;
                }
                if (state.getValue(FenceGateBlock.OPEN)) {
                    model = model == gateWall ? gateWallOpen : gateOpen;
                }
                return ConfiguredModel.builder()
                      .modelFile(model)
                      .rotationY((int) state.getValue(FenceGateBlock.FACING).toYRot())
                      .uvLock(true)
                      .build();
            }, FenceGateBlock.POWERED, fenceGate.value().getFluidLoggedProperty());
        }
    }
}