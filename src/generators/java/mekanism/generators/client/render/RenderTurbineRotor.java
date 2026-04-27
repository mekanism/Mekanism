package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.client.model.ModelTurbine.TurbineBladeRenderState;
import mekanism.generators.client.render.RenderTurbineRotor.TurbineRotorRenderState;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderTurbineRotor extends MekanismTileEntityRenderer<TileEntityTurbineRotor, TurbineRotorRenderState> {

    private static final float BASE_SPEED = 512F;

    private final ModelTurbine model;

    public RenderTurbineRotor(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new ModelTurbine(context.entityModelSet());
    }

    @Override
    public TurbineRotorRenderState createRenderState() {
        return new TurbineRotorRenderState();
    }

    @Override
    public void extractRenderState(TileEntityTurbineRotor rotor, TurbineRotorRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(rotor, state, partialTick, cameraPosition, breakProgress);
        int housedBlades = rotor.getHousedBlades();
        if (housedBlades == 0) {
            return;
        }
        UUID multiblockUUID = rotor.getMultiblockUUID();
        if (multiblockUUID != null) {
            //We are rendering inside the multiblock, use full-bright for the textures
            //TODO - 26.1: Validate that this works
            state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        }

        int baseIndex = rotor.getPosition() * 2;
        if (isTickingNormally(rotor)) {//TODO - 26.1: Re-evaluate where these calculations should be done
            if (multiblockUUID != null && TurbineMultiblockData.clientRotationMap.containsKey(multiblockUUID)) {
                float rotateSpeed = TurbineMultiblockData.clientRotationMap.getFloat(multiblockUUID) * BASE_SPEED;
                rotor.rotationLower += rotateSpeed / (baseIndex + 1);
                rotor.rotationUpper += rotateSpeed / (baseIndex + 2);
            }
            rotor.rotationLower %= 360;
            rotor.rotationUpper %= 360;
        }
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
        submitBlade(state, state.lowerBlade, poseStack, nodeCollector);
        poseStack.popPose();

        //Top blade
        if (state.housedBlades == 2) {
            poseStack.pushPose();
            poseStack.translate(0.5, -0.5, 0.5);
            submitBlade(state, state.upperBlade, poseStack, nodeCollector);
            poseStack.popPose();
        }
    }

    private void submitBlade(TurbineRotorRenderState state, TurbineBladeRenderState bladeState, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        this.model.collect(bladeState, poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, false);
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.TURBINE_ROTOR;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityTurbineRotor tile, Vec3 camera) {
        //TODO - 26.1: See if this renders fine, we used to only use this for when there was no multiblock and had the multiblock render
        // delegate to this renderer with a full bright for when it is formed
        return /*tile.getMultiblockUUID() == null &&*/ tile.getHousedBlades() > 0 && super.shouldRender(tile, camera);
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