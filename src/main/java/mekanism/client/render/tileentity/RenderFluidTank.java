package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.MekanismRenderer.Model3D;
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
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackLinkedSet;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderFluidTank extends MekanismTileEntityRenderer<TileEntityFluidTank, FluidTankRenderState> {

    private static final Map<FluidStack, Int2ObjectMap<Model3D>> cachedCenterFluids = new Object2ObjectOpenCustomHashMap<>(FluidStackLinkedSet.TYPE_AND_COMPONENTS);
    private static final Map<FluidStack, Int2ObjectMap<Model3D>> cachedValveFluids = new Object2ObjectOpenCustomHashMap<>(FluidStackLinkedSet.TYPE_AND_COMPONENTS);

    private static final int stages = 1_400;

    public RenderFluidTank(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static void resetCachedModels() {
        cachedCenterFluids.clear();
        cachedValveFluids.clear();
    }

    @Override
    public FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public void extractRenderState(TileEntityFluidTank tank, FluidTankRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(tank, state, partialTick, cameraPosition, breakProgress);
        //TODO - 26.1: Should we by copying the fluid stacks? - Pup. I think we should pass the texture instead - thiakil
        state.fluid = tank.fluidTank.getFluid();
        state.fluidTint = MekanismRenderer.getColorARGB(state.fluid, state.fluidScale);
        state.fluidGlow = MekanismRenderer.calculateGlowLight(state.lightCoords, state.fluid);
        state.fluidScale = state.fluid.isEmpty() ? 0 : tank.prevScale;
        if (!tank.valveFluid.isEmpty() && !MekanismUtils.lighterThanAirGas(tank.valveFluid)) {
            //If it is lighter than air we don't need to render the valve
            state.valveFluid = tank.valveFluid;
            state.valveTint = MekanismRenderer.getColorARGB(state.valveFluid);
            state.valveGlow = MekanismRenderer.calculateGlowLight(state.lightCoords, state.valveFluid);
        }
    }

    @Override
    public void submit(FluidTankRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        //todo - 26.1 rendering
        //TODO move model gathering to the extract
        RenderType renderType = Sheets.translucentBlockSheet();
        if (state.fluidScale > 0) {
            Model3D object = getFluidModel(state.fluid, state.fluidScale);
            RenderResizableCuboid.renderCube(object, poseStack, renderType, nodeCollector, state.fluidTint, state.fluidGlow, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos));
        }
        if (!state.valveFluid.isEmpty()) {
            Model3D object = getValveModel(state.valveFluid, state.fluidScale);
            RenderResizableCuboid.renderCube(object, poseStack, renderType, nodeCollector, state.valveTint, state.valveGlow, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos));
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.FLUID_TANK;
    }

    private Model3D getValveModel(FluidStack fluid, float fluidScale) {
        Int2ObjectMap<Model3D> modelMap = cachedValveFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>());
        int stage = Math.min(stages - 1, (int) (fluidScale * (stages - 1)));
        Model3D model = modelMap.get(stage);
        if (model == null) {
            model = new Model3D()
                  .setSideRender(side -> side.getAxis().isHorizontal())
                  .prepFlowing(fluid)
                  .xBounds(0.3225F, 0.6775F)
                  .yBounds(0.12375F + 0.7525F * (stage / (float) stages), 0.87625F)
                  .zBounds(0.3225F, 0.6775F);
            modelMap.put(stage, model);
        }
        return model;
    }

    public static Model3D getFluidModel(FluidStack fluid, float fluidScale) {
        Int2ObjectMap<Model3D> modelMap = cachedCenterFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>());
        int stage = ModelRenderer.getStage(fluid, stages, fluidScale);
        Model3D model = modelMap.get(stage);
        if (model == null) {
            model = new Model3D()
                  .setTexture(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL))
                  .setSideRender(Direction.DOWN, false)
                  .setSideRender(Direction.UP, stage < stages)
                  .xBounds(0.135F, 0.865F)
                  .yBounds(0.12375F, 0.124F + 0.75225F * (stage / (float) stages))
                  .zBounds(0.135F, 0.865F);
            modelMap.put(stage, model);
        }
        return model;
    }

    public static class FluidTankRenderState extends BlockEntityRenderState {

        //TODO - 26.1: Store the textures instead of the fluid stacks
        public FluidStack fluid = FluidStack.EMPTY;
        public int fluidTint = 0xFFFFFFFF;
        public int fluidGlow;
        public float fluidScale;
        public FluidStack valveFluid = FluidStack.EMPTY;
        public int valveTint = 0xFFFFFFFF;
        public int valveGlow;
    }
}