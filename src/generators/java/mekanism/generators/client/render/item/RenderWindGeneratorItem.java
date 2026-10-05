package mekanism.generators.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.MekanismAPITags;
import mekanism.client.ModelUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.block.FoilableBlockModelRenderState;
import mekanism.generators.client.model.GeneratorsModelCache;
import mekanism.generators.client.render.RenderWindGenerator;
import mekanism.generators.client.render.item.RenderWindGeneratorItem.GeneratorState;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class RenderWindGeneratorItem implements SpecialModelRenderer<GeneratorState> {

    private static final int SPEED = 16;
    private static long lastTicksUpdated = 0;
    private static int angle = 0;
    private final Lazy<Vector3fc[]> extents = Lazy.of(() -> ModelUtil.computeExtents(GeneratorsBlocks.WIND_GENERATOR));

    private RenderWindGeneratorItem() {
    }

    @Nullable
    @Override
    public GeneratorState extractArgument(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean tickingNormally = MekanismRenderer.isRunningNormally();
        if (tickingNormally && minecraft.level != null) {
            //Only update the angle if we are in a world and that world is not blacklisted
            if (minecraft.level.dimensionTypeRegistration().is(MekanismAPITags.DimensionTypes.NO_WIND)) {
                //If the dimension is blacklisted, don't try to tick it at all
                tickingNormally = false;
            } else {
                long ticks = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.gameTime;
                if (lastTicksUpdated != ticks) {
                    angle = (angle + SPEED) % 360;
                    lastTicksUpdated = ticks;
                }
            }
        }
        GeneratorState state = new GeneratorState();
        state.rotation = angle;
        if (tickingNormally) {
            state.rotation = (state.rotation + SPEED * MekanismRenderer.getPartialTick()) % 360;
        }
        BlockItem itemBlock = (BlockItem) stack.getItem();
        BlockState blockState = itemBlock.getBlock().defaultBlockState();
        Minecraft.getInstance().getBlockModelResolver().update(state.blockRenderState, blockState, ModelUtil.BLOCK_DISPLAY_NO_CONTEXT);
        return state;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (Vector3fc vector3fc : extents.get()) {
            output.accept(vector3fc);
        }
    }

    @Override
    public void submit(@Nullable GeneratorState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) {
            return;
        }
        if (hasFoil) {
            state.blockRenderState.submit(poseStack, nodeCollector, lightCoords, overlayCoords, outlineColor, true);
        } else {
            //Use vanilla's normal rendering submit chain so that if something breaks when updating, we only have things break when using glint
            state.blockRenderState.submit(poseStack, nodeCollector, lightCoords, overlayCoords, outlineColor);
        }
        poseStack.pushPose();
        poseStack.translate(RenderWindGenerator.BLADE_OFFSET);
        poseStack.rotateDegrees(Axis.ZP, state.rotation % 360);
        int[] tints = state.blockRenderState.tintLayers().toArray(BlockModelRenderState.EMPTY_TINTS);
        List<BlockStateModelPart> blades = GeneratorsModelCache.INSTANCE.WIND_GENERATOR_BLADES.getBakedModel();
        nodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockItemSheet(), blades, tints, lightCoords, overlayCoords, outlineColor);
        if (hasFoil) {
            nodeCollector.order(1).submitBlockModel(poseStack, Sheets.cutoutBlockItemGlintSheet(), blades, BlockModelRenderState.EMPTY_TINTS, lightCoords,
                  OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        }
        poseStack.popPose();
    }

    public static class GeneratorState {
        public final FoilableBlockModelRenderState blockRenderState = new FoilableBlockModelRenderState();
        public float rotation;
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<GeneratorState> {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Nullable
        @Override
        public SpecialModelRenderer<GeneratorState> bake(BakingContext context) {
            return new RenderWindGeneratorItem();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}