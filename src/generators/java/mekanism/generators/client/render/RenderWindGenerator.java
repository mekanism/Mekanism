package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.outline.IWireFrameRenderer;
import mekanism.client.render.outline.Outlines;
import mekanism.client.render.outline.Outlines.Line;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.GeneratorsModelCache;
import mekanism.generators.client.render.RenderWindGenerator.WindGeneratorRenderState;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderWindGenerator extends MekanismTileEntityRenderer<TileEntityWindGenerator, WindGeneratorRenderState> implements IWireFrameRenderer {

    public static final Vec3 BLADE_OFFSET = new Vec3(0.5, 4.5, 0.5);
    @Nullable
    private static List<Line> lines;

    public static void resetCached() {
        lines = null;
    }

    @Override
    public WindGeneratorRenderState createRenderState() {
        return new WindGeneratorRenderState();
    }

    @Override
    public void extractRenderState(TileEntityWindGenerator generator, WindGeneratorRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(generator, state, partialTick, cameraPosition, breakProgress);
        state.direction = generator.getDirection();
        state.rotation = generator.getAngle();
        if (generator.getActive() && partialTick > 0) {
            state.rotation = (state.rotation + generator.getHeightSpeedRatio() * partialTick) % 360;
        }
        if (generator.getLevel() != null) {
            //Have the blades use the light level of the top bounding block where the rotor is
            state.lightCoords = LightCoordsUtil.getLightCoords(generator.getLevel(), state.blockPos.above(4));
        }
    }

    @Override
    public void submit(WindGeneratorRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.direction != null) {
            poseStack.pushPose();
            poseStack.translate(BLADE_OFFSET);
            MekanismRenderer.rotate(poseStack, state.direction, 0, 180, 90, 270);
            poseStack.rotateDegrees(Axis.ZP, state.rotation % 360);
            submitBreakableBlockModel(nodeCollector, poseStack, Sheets.cutoutBlockItemSheet(), GeneratorsModelCache.INSTANCE.WIND_GENERATOR_BLADES.getBakedModel(), state);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityWindGenerator tile) {
        //Note: we just extend it to the max size (including blades) it could be for the direction it is facing
        BlockPos pos = tile.getBlockPos();
        Direction direction = tile.getDirection();
        return (switch (direction) {
            case NORTH, SOUTH -> AABB.encapsulatingFullBlocks(pos.offset(-2, 2, 0), pos.offset(2, 6, 0))
                  .deflate(0.4);
            case EAST, WEST -> AABB.encapsulatingFullBlocks(pos.offset(0, 2, -2), pos.offset(0, 6, 2))
                  .deflate(0.4);
            //This should never be the case
            default -> AABB.encapsulatingFullBlocks(pos.offset(-2, 2, -2), pos.offset(2, 6, 2))
                  .deflate(0.4);
        }).move(0.375 * direction.getStepX(), 0, 0.375 * direction.getStepZ());
    }

    @Override
    public Collection<Line> applyTransformAndGetFrame(BlockEntity tile, float partialTick, PoseStack poseStack, LevelRenderState levelRenderState) {
        if (!(tile instanceof TileEntityWindGenerator generator)) {
            return Collections.emptyList();
        } else if (lines == null) {
            lines = Outlines.extract(GeneratorsModelCache.INSTANCE.WIND_GENERATOR_BLADES.getBakedModel());
        }
        poseStack.translate(BLADE_OFFSET);
        MekanismRenderer.rotate(poseStack, generator.getDirection(), 0, 180, 90, 270);
        float angle;
        if (generator.getActive() && partialTick > 0) {
            angle = (generator.getAngle() + generator.getHeightSpeedRatio() * partialTick) % 360;
        } else {
            angle = generator.getAngle();
        }
        poseStack.rotateDegrees(Axis.ZP, angle % 360);
        return lines;
    }

    public static class WindGeneratorRenderState extends BlockEntityRenderState {

        public float rotation = 0;
        @Nullable
        public Direction direction;
    }
}