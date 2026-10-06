package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.client.model.ModelTurbine.TurbineBladeRenderState;
import mekanism.generators.client.render.RenderTurbineRotor.TurbineRotorRenderState;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderTurbineRotor extends MekanismTileEntityRenderer<TileEntityTurbineRotor, TurbineRotorRenderState> {

    public static final float BASE_SPEED = 512F;
    @Nullable
    public static RenderTurbineRotor INSTANCE;

    private final ModelTurbine model;

    public RenderTurbineRotor(BlockEntityRendererProvider.Context context) {
        this.model = new ModelTurbine(context.entityModelSet());
        INSTANCE = this;
    }

    @Override
    public TurbineRotorRenderState createRenderState() {
        return new TurbineRotorRenderState();
    }

    @Override
    public void extractRenderState(TileEntityTurbineRotor rotor, TurbineRotorRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(rotor, state, partialTick, cameraPosition, breakProgress);
        state.housedBlades = rotor.getHousedBlades();
        if (state.housedBlades == 0) {//Sanity check
            return;
        }
        int baseIndex = rotor.getPosition() * 2;
        state.lowerBlade.index = baseIndex;
        state.lowerBlade.rotation = rotor.rotationLower;

        state.upperBlade.index = baseIndex + 1;
        state.upperBlade.rotation = rotor.rotationUpper;
    }

    @Override
    public void submit(TurbineRotorRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.housedBlades == 0) {//No blades, nothing to render
            return;
        }

        //Bottom blade
        poseStack.pushPose();
        poseStack.translate(0.5, -1, 0.5);
        poseStack.rotateDegrees(Axis.YP, state.lowerBlade.rotation);
        submitCrumblingModel(nodeCollector, this.model, state.lowerBlade, poseStack, this.model.getRenderType(), state);
        poseStack.popPose();

        //Top blade
        if (state.housedBlades == 2) {
            poseStack.pushPose();
            poseStack.translate(0.5, -0.5, 0.5);
            poseStack.rotateDegrees(Axis.YP, state.upperBlade.rotation);
            submitCrumblingModel(nodeCollector, this.model, state.upperBlade, poseStack, this.model.getRenderType(), state);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityTurbineRotor tile, Vec3 camera) {
        //Note: When a multiblock is present, we let the turbine handle the rendering so that it can calculate the light level properly
        return tile.getHousedBlades() > 0 && tile.getMultiblockUUID() == null && super.shouldRender(tile, camera);
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityTurbineRotor tile) {
        int radius = tile.getRadius();
        if (tile.blades == 0 || radius == -1) {
            //If there are no blades default to the collision box of the rotor
            return super.getRenderBoundingBox(tile);
        }
        BlockPos pos = tile.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-radius, 0, -radius), pos.offset(radius, 0, radius));
    }

    public static class TurbineRotorRenderState extends BlockEntityRenderState {

        public TurbineBladeRenderState lowerBlade = new TurbineBladeRenderState();
        public TurbineBladeRenderState upperBlade = new TurbineBladeRenderState();
        public int housedBlades;
    }
}