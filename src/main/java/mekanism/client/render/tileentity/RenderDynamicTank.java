package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.LightCoordsUtil;
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
        state.tankTexture = MekanismRenderer.getSinglePicker(getContentsTexture(multiblock));
        state.scale = multiblock.prevScale;
        state.valves.clear();
        state.valveTexture = null;
        if (state.renderData instanceof FluidRenderData fluidRenderData) {
            state.valveTexture = MekanismRenderer.getValveTexture(multiblock.getFluidTank().getFluid());
            for (IValveHandler.ValveData valve : multiblock.valves) {//todo - 26.1: are these always active? (when not empty) Should they be?
                state.valves.add(ValveRenderData.get(fluidRenderData, valve));
            }
        }
    }

    @Override
    public void submit(DynamicTankRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        RenderType renderType = Sheets.translucentBlockSheet();
        if (state.renderData instanceof FluidRenderData fluidRenderData) {
            MekanismRenderer.Model3D fluidModel = ModelRenderer.getModel(fluidRenderData, state.scale);
            int fluidColor = fluidRenderData.getColorARGB();
            int fluidColorScaled = fluidRenderData.getColorARGB(state.scale);
            int glowLight = fluidRenderData.calculateGlowLight(LightCoordsUtil.FULL_SKY);
            RenderResizableCuboid.renderObject(camera.pos, poseStack, renderType, nodeCollector, fluidModel, state.tankTexture, OverlayTexture.NO_OVERLAY, glowLight, fluidColorScaled, state.blockPos, fluidRenderData.location, fluidRenderData.length, fluidRenderData.width, fluidRenderData.height);
            RenderResizableCuboid.renderValves(camera.pos, poseStack, renderType, nodeCollector, fluidModel, state.valves, OverlayTexture.NO_OVERLAY, state.valveTexture, state.blockPos, fluidRenderData.location, fluidRenderData.length, fluidRenderData.width, fluidRenderData.height, fluidColor, glowLight);
        } else if (state.renderData != null) {
            MekanismRenderer.Model3D model = ModelRenderer.getModel(state.renderData, state.scale);
            RenderResizableCuboid.renderObject(camera.pos, poseStack, renderType, nodeCollector, model, state.tankTexture, OverlayTexture.NO_OVERLAY, state.renderData.calculateGlowLight(LightCoordsUtil.FULL_SKY), state.renderData.getColorARGB(state.scale), state.blockPos, state.renderData.location, state.renderData.length, state.renderData.width, state.renderData.height);
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

    @Nullable
    private TextureAtlasSprite getContentsTexture(TankMultiblockData multiblock) {
        CurrentType currentType = multiblock.mergedTank.getCurrentType();
        if (currentType == CurrentType.EMPTY) {
            return null;
        }
        return switch (currentType) {
            case FLUID -> MekanismRenderer.getFluidTexture(multiblock.getFluidTank().getFluid(), MekanismRenderer.FluidTextureType.STILL);
            case CHEMICAL -> MekanismRenderer.getChemicalTexture(multiblock.getChemicalTank().getStack());
            default -> throw new IllegalStateException("Unknown current type.");
        };
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
        public @Nullable RenderResizableCuboid.TexturePicker tankTexture;
        @Nullable
        public MekanismRenderer.ValveTextureGetter valveTexture;
    }
}