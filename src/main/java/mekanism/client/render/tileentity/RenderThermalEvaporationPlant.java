package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.tileentity.RenderThermalEvaporationPlant.TEPRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
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
        state.scale = Math.min(1, multiblock.prevScale);
        FluidResource fluid = multiblock.inputTank.resource();
        state.data = RenderData.Builder.create(multiblock.inputTank)
              .of(multiblock)
              .height(multiblock.height() - 1)
              .build();
        state.fluidTexture = fluid.isEmpty() ? null : MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
        state.valveTexture = fluid.isEmpty() ? null : MekanismRenderer.getValveTexture(fluid);
        state.valves.clear();
        for (Map.Entry<BlockPos, IValveHandler.ValveData> entry : multiblock.valves.entrySet()) {//todo - 26.1: are these always active? (when not empty) Should they be?
            state.valves.add(ValveRenderData.get(state.data, entry.getKey(), entry.getValue().side));
        }
    }

    @Override
    public void submit(TEPRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        //todo - 26.1: rendering
        if (state.data != null) {
            RenderType renderType = Sheets.translucentBlockSheet();
            MekanismRenderer.Model3D fluidModel = ModelRenderer.getModel(state.data, state.scale);
            int fluidColor = state.data.getColorARGB();
            int fluidColorScaled = state.data.getColorARGB(state.scale);
            int glowLight = state.data.calculateGlowLight(LightCoordsUtil.FULL_SKY);
            RenderResizableCuboid.renderObject(camera.pos, poseStack, renderType, nodeCollector, fluidModel, state.fluidTexture, OverlayTexture.NO_OVERLAY, glowLight, fluidColorScaled, state.blockPos, state.data.location, state.data.length, state.data.width, state.data.height);
            RenderResizableCuboid.renderValves(camera.pos, poseStack, renderType, nodeCollector, fluidModel, state.valves, OverlayTexture.NO_OVERLAY, state.valveTexture, state.blockPos, state.data.location, state.data.length, state.data.width, state.data.height, fluidColor, glowLight);
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

    public static class TEPRenderState extends BlockEntityRenderState {

        @Nullable
        public FluidRenderData data;
        public float scale;
        public List<ValveRenderData> valves = new ArrayList<>();
        @Nullable
        public RenderResizableCuboid.TexturePicker fluidTexture;
        @Nullable
        public MekanismRenderer.ValveTextureGetter valveTexture;

    }
}