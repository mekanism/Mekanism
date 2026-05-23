package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.MultiblockContentsRenderState;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.tileentity.RenderDynamicTank.DynamicTankRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
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
        state.gather(multiblock);

        float scale = multiblock.prevScale;
        state.valves.clear();
        state.valveTexture = null;

        switch (multiblock.mergedTank.getCurrentType()) {
            case FLUID -> {
                FluidStack fluid = multiblock.getFluidTank().getFluid();
                state.tankTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
                state.tankGlow = MekanismRenderer.calculateGlowLight(LightCoordsUtil.FULL_SKY, fluid);
                state.tankColor = MekanismRenderer.getColorARGB(fluid, scale);
                state.tankMaxY = ModelRenderer.getMaxY(state.height, scale, MekanismUtils.lighterThanAirGas(fluid));
                state.valveTexture = MekanismRenderer.getValveTexture(fluid);
                for (IValveHandler.ValveData valve : multiblock.valves) {//todo - 26.1: are these always active? (when not empty) Should they be?
                    state.valves.add(ValveRenderData.get(valve, state.tankMaxY - 0.01F, state.renderLocation, state.height));
                }
            }
            case CHEMICAL -> {
                ChemicalStack chemical = multiblock.getChemicalTank().getStack();
                state.tankTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(chemical));
                state.tankGlow = LightCoordsUtil.FULL_SKY;
                state.tankColor = MekanismRenderer.getColorARGB(chemical, scale);
                state.tankMaxY = ModelRenderer.getMaxY(state.height, scale, chemical.is(MekanismAPITags.Chemicals.GASEOUS));
            }
            case EMPTY -> {
                state.tankTexture = null;
            }
        }
    }

    @Override
    public void submit(DynamicTankRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.tankTexture == null) {
            return;
        }
        RenderType renderType = Sheets.translucentBlockSheet();
        RenderResizableCuboid.renderObject(camera.pos, poseStack, renderType, nodeCollector, MekanismRenderer.TMP_SideRenderCheck.RENDER_ALL, 0.01F, 0.01F, 0.01F, state.length - 0.02F, state.tankMaxY, state.width - 0.02F, state.tankTexture, OverlayTexture.NO_OVERLAY, state.tankGlow, state.tankColor, state.blockPos, state.renderLocation, state.length, state.width, state.height);
        if (!state.valves.isEmpty()) {//redundant, but saves some stack space
            RenderResizableCuboid.renderValves(camera.pos, poseStack, renderType, nodeCollector, state.valves, OverlayTexture.NO_OVERLAY, state.valveTexture, state.blockPos, state.renderLocation, state.length, state.width, state.height, state.tankColor, state.tankGlow, state.tankMaxY - 0.01F);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DYNAMIC_TANK;
    }

    @Override
    protected boolean shouldRender(TileEntityDynamicTank tile, TankMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && !multiblock.isEmpty();
    }

    public static class DynamicTankRenderState extends MultiblockContentsRenderState {

        @Nullable
        public RenderResizableCuboid.TexturePicker tankTexture;
        public int tankColor;
        public int tankGlow;
        public float tankMaxY;

        public List<ValveRenderData> valves = new ArrayList<>();
        @Nullable
        public MekanismRenderer.ValveTextureGetter valveTexture;

    }
}