package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.tileentity.RenderFluidTank.FluidTankRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderFluidTank extends MekanismTileEntityRenderer<TileEntityFluidTank, FluidTankRenderState> {

    private static final int stages = 1_400;
    public static final float CONTENTS_MIN_XZ = 0.135F;
    public static final float CONTENTS_MAX_XZ = 0.865F;
    public static final float CONTENTS_MIN_Y = 0.12375F;
    public static final float VALVE_MIN_XZ = 0.3225F;
    public static final float VALVE_MAX_XZ = 0.6775F;
    public static final float VALVE_MAX_Y = 0.87625F;

    public RenderFluidTank(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public void extractRenderState(TileEntityFluidTank tank, FluidTankRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(tank, state, partialTick, cameraPosition, breakProgress);
        FluidStack fluid = tank.fluidTank.getFluid();
        state.fluidTint = MekanismRenderer.getColorARGB(fluid, state.fluidScale);
        state.fluidGlow = MekanismRenderer.calculateGlowLight(state.lightCoords, fluid);
        state.fluidScale = fluid.isEmpty() ? 0 : tank.prevScale;
        boolean gaseous = MekanismUtils.lighterThanAirGas(fluid);
        state.contentsMaxY = state.fluidScale > 0 ? contentsMaxY(state.fluidScale, gaseous) : 0;
        state.fluidTexture = fluid.isEmpty() ? null : MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL));

        if (!tank.valveFluid.isEmpty() && !gaseous) {
            //If it is lighter than air we don't need to render the valve
            FluidStack valveFluid = tank.valveFluid;
            state.valveMinY = valveMinY(state.fluidScale);
            state.valveTint = MekanismRenderer.getColorARGB(valveFluid);
            state.valveGlow = MekanismRenderer.calculateGlowLight(state.lightCoords, valveFluid);
            state.valveFluidTexture = MekanismRenderer.getValveTexture(valveFluid);
        } else {
            state.valveFluidTexture = null;
        }
    }

    @Override
    public void submit(FluidTankRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        RenderType renderType = Sheets.translucentBlockSheet();
        if (state.fluidScale > 0) {
            RenderResizableCuboid.renderCube(RenderResizableCuboid.TMP_SideRenderCheck.NOT_DOWN, CONTENTS_MIN_XZ, CONTENTS_MIN_Y, CONTENTS_MIN_XZ, CONTENTS_MAX_XZ, state.contentsMaxY, CONTENTS_MAX_XZ, poseStack, renderType, nodeCollector, state.fluidTint, state.fluidGlow, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos), state.fluidTexture);
        }
        if (state.valveFluidTexture != null) {
            RenderResizableCuboid.renderCube(RenderResizableCuboid.TMP_SideRenderCheck.HORIZONTAL, VALVE_MIN_XZ, state.valveMinY, VALVE_MIN_XZ, VALVE_MAX_XZ, VALVE_MAX_Y, VALVE_MAX_XZ, poseStack, renderType, nodeCollector, state.valveTint, state.valveGlow, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos), state.valveFluidTexture);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.FLUID_TANK;
    }

    public static float valveMinY(float fluidScale) {
        int stageToUse = Math.min(stages - 1, (int) (fluidScale * (stages - 1)));
        float stageFraction = stageToUse / (float) stages;
        return CONTENTS_MIN_Y + 0.7525F * stageFraction;
    }

    public static float contentsMaxY(float fluidScale, boolean gaseous) {
        int stage = ModelRenderer.getStage(gaseous, stages, fluidScale);
        return CONTENTS_MIN_Y + 0.75225F * (stage / (float) stages);
    }

    public static class FluidTankRenderState extends BlockEntityRenderState {

        public float contentsMaxY;
        public int fluidTint = 0xFFFFFFFF;
        public int fluidGlow;
        public float fluidScale;
        public float valveMinY;
        public int valveTint = 0xFFFFFFFF;
        public int valveGlow;
        @Nullable
        public RenderResizableCuboid.TexturePicker fluidTexture;
        @Nullable
        public MekanismRenderer.ValveTextureGetter valveFluidTexture;

    }
}