package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.MultiblockContentsRenderState;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler.BoilerRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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
        state.gather(multiblock);

        float waterScale = multiblock.waterTank.isEmpty() ? 0 : multiblock.prevWaterScale;
        float steamScale = multiblock.steamTank.isEmpty() ? 0 : multiblock.prevSteamScale;

        if (multiblock.renderLocation == null || multiblock.upperRenderLocation == null) {
            return;
        }

        int height = multiblock.upperRenderLocation.getY() - 1 - multiblock.renderLocation.getY();
        if (height > 0) {
            FluidResource fluid = multiblock.waterTank.resource();
            state.height = height;
            state.waterTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
            state.valveTexture = MekanismRenderer.getValveTexture(fluid);
            state.waterGlow = MekanismRenderer.calculateGlowLight(LightCoordsUtil.FULL_SKY, fluid);
            state.waterColor = MekanismRenderer.getColorARGB(fluid, waterScale);
            state.waterMaxY = ModelRenderer.getMaxY(state.height, waterScale, MekanismUtils.lighterThanAirGas(fluid));
            state.valves.clear();
            if (waterScale > 0) {
                for (Map.Entry<BlockPos, IValveHandler.ValveData> entry : multiblock.valves.entrySet()) {//todo - 26.1: are these always active? (when not empty) Should they be?
                    state.valves.add(ValveRenderData.get(entry.getValue(), entry.getKey(), state.waterMaxY - 0.01F, state.renderLocation, state.height));
                }
            }
        } else {
            state.waterTexture = null;
            state.valveTexture = null;
        }

        int steamHeight = multiblock.renderLocation.getY() + multiblock.height() - 2 - multiblock.upperRenderLocation.getY();
        state.steamHeight = steamHeight;
        if (steamHeight > 0) {
            state.upperRenderLocation = multiblock.upperRenderLocation.offset(1, 0, 1);
            ChemicalResource chemicalType = multiblock.steamTank.resource();
            state.steamTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(chemicalType));
            state.steamColor = MekanismRenderer.getColorARGB(chemicalType, steamScale);
            state.steamMaxY = ModelRenderer.getMaxY(steamHeight, steamScale, chemicalType.is(MekanismAPITags.Chemicals.GASEOUS));
        } else {
            state.steamTexture = null;
        }
    }

    @Override
    public void submit(BoilerRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        RenderType renderType = Sheets.translucentBlockSheet();
        if (state.waterTexture != null) {
            RenderResizableCuboid.renderObject(camera.pos, poseStack, renderType, nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES, 0.01F, 0.01F, 0.01F, state.length - 0.02F, state.waterMaxY, state.width - 0.02F, state.waterTexture, OverlayTexture.NO_OVERLAY, state.waterGlow, state.waterColor, state.blockPos, state.renderLocation, state.length, state.width, state.height);
            RenderResizableCuboid.renderValves(camera.pos, poseStack, renderType, nodeCollector, state.valves, OverlayTexture.NO_OVERLAY, state.valveTexture, state.blockPos, state.renderLocation, state.length, state.width, state.height, state.waterColor, state.waterGlow, state.waterMaxY - 0.01F);
        }
        if (state.steamTexture != null) {
            RenderResizableCuboid.renderObject(camera.pos, poseStack, renderType, nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES, 0.01F, 0.01F, 0.01F, state.length - 0.02F, state.steamMaxY, state.width - 0.02F, state.steamTexture, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_SKY, state.steamColor, state.blockPos, state.upperRenderLocation, state.length, state.width, state.steamHeight);
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

    public static class BoilerRenderState extends MultiblockContentsRenderState {

        public BlockPos upperRenderLocation = BlockPos.ZERO;

        @Nullable
        public RenderResizableCuboid.TexturePicker waterTexture;
        public int waterColor;
        public int waterGlow;
        public float waterMaxY;

        @Nullable
        public RenderResizableCuboid.TexturePicker steamTexture;
        public float steamMaxY;
        public int steamColor;
        public int steamHeight;

        public List<ValveRenderData> valves = new ArrayList<>();
        @Nullable
        public MekanismRenderer.ValveTextureGetter valveTexture;
    }
}