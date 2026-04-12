package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler.BoilerRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderThermoelectricBoiler extends MultiblockTileEntityRenderer<BoilerMultiblockData, TileEntityBoilerCasing, BoilerRenderState> {

    public RenderThermoelectricBoiler(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BoilerRenderState createRenderState() {
        return new BoilerRenderState();
    }

    @Override
    public void extractRenderState(TileEntityBoilerCasing boiler, BoilerRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(boiler, state, partialTick, cameraPosition, breakProgress);
        BoilerMultiblockData multiblock = boiler.getMultiblock();
        state.waterScale = multiblock.waterTank.isEmpty() ? 0 :multiblock.prevWaterScale;
        state.steamScale = multiblock.steamTank.isEmpty() ? 0 :multiblock.prevSteamScale;
        if (multiblock.renderLocation == null || multiblock.upperRenderLocation == null) {
            return;
        }
        int height = multiblock.upperRenderLocation.getY() - 1 - multiblock.renderLocation.getY();
        if (height > 0) {
            state.waterData = RenderData.Builder.create(multiblock.waterTank.getFluid())
                  .of(multiblock)
                  .height(height)
                  .build();
        }
        int steamHeight = multiblock.renderLocation.getY() + multiblock.height() - 2 - multiblock.upperRenderLocation.getY();
        if (steamHeight > 0) {
            state.steamData = RenderData.Builder.create(multiblock.steamTank.getStack())
                  .of(multiblock)
                  .location(multiblock.upperRenderLocation.offset(1, 0, 1))
                  .height(steamHeight)
                  .build();
        }
    }

    @Override
    public void submit(BoilerRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.waterScale > 0 && state.waterData != null) {
            renderObject(camera.pos, state.waterData, state.valves, state.blockPos, poseStack, Sheets.translucentCullBlockSheet(), OverlayTexture.NO_OVERLAY, state.waterScale);
        }
        if (state.steamScale > 0 && state.steamData != null) {
            renderObject(camera.pos, state.steamData, state.blockPos, poseStack, Sheets.translucentCullBlockSheet(), OverlayTexture.NO_OVERLAY, state.steamScale);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMOELECTRIC_BOILER;
    }

    @Override
    protected boolean shouldRender(TileEntityBoilerCasing tile, BoilerMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && multiblock.upperRenderLocation != null;
    }

    public static class BoilerRenderState extends BlockEntityRenderState {

        @Nullable
        public FluidRenderData waterData;
        public float waterScale;
        @Nullable
        public RenderData steamData;
        public float steamScale;
    }
}