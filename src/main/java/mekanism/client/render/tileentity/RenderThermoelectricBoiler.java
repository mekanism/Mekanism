package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler.BoilerRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
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
            FluidStack fluid = multiblock.waterTank.getFluid();
            state.waterData = RenderData.Builder.create(fluid)
                  .of(multiblock)
                  .height(height)
                  .build();
            state.waterTexture = MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL);
            state.valveTexture = MekanismRenderer.getValveTexture(fluid);
        } else {
            state.waterData = null;
            state.waterTexture = null;
            state.valveTexture = null;
        }
        int steamHeight = multiblock.renderLocation.getY() + multiblock.height() - 2 - multiblock.upperRenderLocation.getY();
        if (steamHeight > 0) {
            ChemicalStack chemicalStack = multiblock.steamTank.getStack();
            state.steamData = RenderData.Builder.create(chemicalStack)
                  .of(multiblock)
                  .location(multiblock.upperRenderLocation.offset(1, 0, 1))
                  .height(steamHeight)
                  .build();
            state.steamTexture = MekanismRenderer.getChemicalTexture(chemicalStack);
        } else {
            state.steamData = null;
        }
        state.valves.clear();
        if (state.waterScale > 0 && state.waterData != null) {
            for (IValveHandler.ValveData valve : multiblock.valves) {//todo - 26.1: are these always active? (when not empty) Should they be?
                state.valves.add(ValveRenderData.get(state.waterData, valve));
            }
        }
    }

    @Override
    public void submit(BoilerRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        //todo - 26.1: rendering
        RenderType renderType = Sheets.translucentBlockSheet();
        if (state.waterScale > 0 && state.waterData != null) {
            MekanismRenderer.Model3D fluidModel = ModelRenderer.getModel(state.waterData, state.waterScale);
            RenderResizableCuboid.renderObject(camera.pos, state.waterData, state.valves, state.blockPos, poseStack, renderType, nodeCollector, OverlayTexture.NO_OVERLAY, state.waterScale, fluidModel, state.waterTexGetter, state.valveTexture);
        }
        if (state.steamScale > 0 && state.steamData != null) {
            MekanismRenderer.Model3D model = ModelRenderer.getModel(state.steamData, state.steamScale);
            RenderResizableCuboid.renderObject(camera.pos, state.steamData, state.blockPos, model, poseStack, renderType, nodeCollector, OverlayTexture.NO_OVERLAY, state.steamScale, state.steamTexGetter);
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
        @Nullable
        public TextureAtlasSprite waterTexture;
        public Function<Direction, TextureAtlasSprite> waterTexGetter = _ -> waterTexture;
        public float waterScale;
        @Nullable
        public RenderData steamData;
        @Nullable
        public TextureAtlasSprite steamTexture;
        public Function<Direction, TextureAtlasSprite> steamTexGetter = _ -> steamTexture;
        public float steamScale;
        public List<ValveRenderData> valves = new ArrayList<>();
        @Nullable
        public MekanismRenderer.ValveTextureGetter valveTexture;
    }
}