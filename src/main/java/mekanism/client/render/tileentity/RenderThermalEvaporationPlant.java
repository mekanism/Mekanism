package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.MultiblockContentsRenderState;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.tileentity.RenderThermalEvaporationPlant.TEPRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderThermalEvaporationPlant extends MultiblockTileEntityRenderer<EvaporationMultiblockData, TileEntityThermalEvaporationController, TEPRenderState> {

    public RenderThermalEvaporationPlant(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TEPRenderState createRenderState() {
        return new TEPRenderState();
    }

    @Override
    public void extractRenderState(TileEntityThermalEvaporationController controller, TEPRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(controller, state, partialTick, cameraPosition, breakProgress);
        EvaporationMultiblockData multiblock = controller.getMultiblock();
        state.gather(multiblock);

        float scale = Math.min(1, multiblock.prevScale);
        FluidStack fluid = multiblock.inputTank.getFluid();

        state.fluidTexture = fluid.isEmpty() ? null : MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
        state.valveTexture = fluid.isEmpty() ? null : MekanismRenderer.getValveTexture(fluid);
        state.tankGlow = MekanismRenderer.calculateGlowLight(LightCoordsUtil.FULL_SKY, fluid);
        state.tankColor = MekanismRenderer.getColorARGB(fluid, scale);
        state.tankMaxY = ModelRenderer.getMaxY(state.height, scale, MekanismUtils.lighterThanAirGas(fluid));
        state.valves.clear();
        for (IValveHandler.ValveData valve : multiblock.valves) {//todo - 26.1: are these always active? (when not empty) Should they be?
            state.valves.add(ValveRenderData.get(valve, state.tankMaxY - 0.01F, state.renderLocation, state.height));
        }
    }

    @Override
    public void submit(TEPRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.fluidTexture != null) {
            RenderType renderType = Sheets.translucentBlockSheet();
            RenderResizableCuboid.renderObject(camera.pos, poseStack, renderType, nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES, 0.01F, 0.01F, 0.01F, state.length - 0.02F, state.tankMaxY, state.width - 0.02F, state.fluidTexture, OverlayTexture.NO_OVERLAY, state.tankGlow, state.tankColor, state.blockPos, state.renderLocation, state.length, state.width);
            if (!state.valves.isEmpty()) {
                RenderResizableCuboid.renderValves(camera.pos, poseStack, renderType, nodeCollector, state.valves, OverlayTexture.NO_OVERLAY, state.valveTexture, state.blockPos, state.renderLocation, state.length, state.width, state.height, state.tankColor, state.tankGlow, state.tankMaxY - 0.01F);
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMAL_EVAPORATION_CONTROLLER;
    }

    @Override
    protected boolean shouldRender(TileEntityThermalEvaporationController tile, EvaporationMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && !multiblock.inputTank.isEmpty();
    }

    public static class TEPRenderState extends MultiblockContentsRenderState {
        
        public int tankColor;
        public int tankGlow;
        public float tankMaxY;
        public List<ValveRenderData> valves = new ArrayList<>();
        @Nullable
        public RenderResizableCuboid.TexturePicker fluidTexture;
        @Nullable
        public MekanismRenderer.ValveTextureGetter valveTexture;

    }
}