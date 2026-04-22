package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.tileentity.RenderThermalEvaporationPlant.TEPRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
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
        state.valves = new HashSet<>(multiblock.valves);
        state.data = RenderData.Builder.create(multiblock.inputTank.getFluid())
              .of(multiblock)
              .height(multiblock.height() - 1)
              .build();
    }

    @Override
    public void submit(TEPRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        //todo - 26.1: rendering
        //renderObject(camera.pos, state.data, state.valves, state.blockPos, poseStack, Sheets.translucentCullBlockSheet(), OverlayTexture.NO_OVERLAY, state.scale);
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
        public Set<ValveData> valves;
    }
}