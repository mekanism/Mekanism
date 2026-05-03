package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.tileentity.RenderDynamicTank.DynamicTankRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderDynamicTank extends MultiblockTileEntityRenderer<TankMultiblockData, TileEntityDynamicTank, DynamicTankRenderState> {

    public RenderDynamicTank(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DynamicTankRenderState createRenderState() {
        return new DynamicTankRenderState();
    }

    @Override
    public void extractRenderState(TileEntityDynamicTank tank, DynamicTankRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(tank, state, partialTick, cameraPosition, breakProgress);
        TankMultiblockData multiblock = tank.getMultiblock();
        state.renderData = getRenderData(multiblock);
        state.scale = multiblock.prevScale;
        state.valves.clear();
        if (state.renderData instanceof FluidRenderData fluidRenderData) {
            for (IValveHandler.ValveData valve : multiblock.valves) {//todo - 26.1: are these always active? (when not empty) Should they be?
                state.valves.add(ValveRenderData.get(fluidRenderData, valve));
            }
        }
    }

    @Override
    public void submit(DynamicTankRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.renderData instanceof FluidRenderData fluidRenderData) {
            RenderResizableCuboid.renderObject(camera.pos, fluidRenderData, state.valves, state.blockPos, poseStack, Sheets.translucentBlockSheet(), nodeCollector, OverlayTexture.NO_OVERLAY, state.scale);
        } else if (state.renderData != null) {
            RenderResizableCuboid.renderObject(camera.pos, state.renderData, state.blockPos, poseStack, Sheets.translucentBlockSheet(), nodeCollector, OverlayTexture.NO_OVERLAY, state.scale);
        }
    }

    @Nullable
    private RenderData getRenderData(TankMultiblockData multiblock) {
        CurrentType currentType = multiblock.mergedTank.getCurrentType();
        if (currentType == CurrentType.EMPTY) {
            return null;
        }
        return (switch (currentType) {
            case FLUID -> RenderData.Builder.create(multiblock.getFluidTank().getFluid());
            case CHEMICAL -> RenderData.Builder.create(multiblock.getChemicalTank().getStack());
            default -> throw new IllegalStateException("Unknown current type.");
        }).of(multiblock).build();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DYNAMIC_TANK;
    }

    @Override
    protected boolean shouldRender(TileEntityDynamicTank tile, TankMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && !multiblock.isEmpty();
    }

    public static class DynamicTankRenderState extends BlockEntityRenderState {

        @Nullable
        public RenderData renderData;
        public float scale;
        public List<ValveRenderData> valves = new ArrayList<>();
    }
}