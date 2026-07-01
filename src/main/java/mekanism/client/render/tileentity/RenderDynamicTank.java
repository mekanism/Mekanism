package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalResource;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class RenderDynamicTank extends MultiblockTileEntityRenderer<TankMultiblockData, TileEntityDynamicTank, DynamicTankRenderState> {

    public RenderDynamicTank(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DynamicTankRenderState createRenderState() {
        return new DynamicTankRenderState();
    }

    @Override
    public void extractRenderState(TileEntityDynamicTank tank, TankMultiblockData multiblock, DynamicTankRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        float scale = multiblock.prevScale;
        switch (multiblock.mergedTank.getCurrentType()) {
            case FLUID -> {
                FluidResource fluid = multiblock.getFluidTank().resource();
                state.calculateLightCoords(tank.getLevel(), multiblock, fluid.getFluidType().getLightLevel());
                state.tankTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL));
                state.tankColor = MekanismRenderer.getColorARGB(fluid, scale);
                state.tankMaxY = ModelRenderer.getMaxY(state.height, scale, MekanismUtils.lighterThanAirGas(fluid));
                state.valveTexture = MekanismRenderer.getValveTexture(fluid);
                for (Map.Entry<BlockPos, IValveHandler.ValveData> entry : multiblock.valves.entrySet()) {//TODO - 26.2: are these always active? (when not empty) Should they be?
                    state.valves.add(ValveRenderData.get(entry.getValue(), entry.getKey(), state.tankMaxY - 0.01F, state.renderLocation, state.height));
                }
            }
            case CHEMICAL -> {
                ChemicalResource chemical = multiblock.getChemicalTank().resource();
                state.calculateLightCoords(tank.getLevel(), multiblock, chemical.value().lightLevel());
                state.tankTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(chemical));
                state.tankColor = MekanismRenderer.getColorARGB(chemical, scale);
                state.tankMaxY = ModelRenderer.getMaxY(state.height, scale, chemical.is(MekanismAPITags.Chemicals.GASEOUS));
            }
        }
    }

    @Override
    public void submit(DynamicTankRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.tankTexture == null) {
            return;
        }
        RenderType renderType = Sheets.translucentBlockItemSheet();
        RenderResizableCuboid.renderObject(camera.pos, poseStack, renderType, nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES,
              0.01F, 0.01F, 0.01F, state.length - 0.02F, state.tankMaxY, state.width - 0.02F, state.tankTexture,
              OverlayTexture.NO_OVERLAY, state.lightCoords, state.tankColor, state.blockPos, state.renderLocation, state.length, state.width);
        if (!state.valves.isEmpty() && state.valveTexture != null) {//redundant, but saves some stack space
            RenderResizableCuboid.renderValves(camera.pos, poseStack, renderType, nodeCollector, state.valves, OverlayTexture.NO_OVERLAY, state.valveTexture,
                  state.blockPos, state.renderLocation, state.length, state.width, state.height, state.tankColor, state.lightCoords, state.tankMaxY - 0.01F);
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

        public RenderResizableCuboid.@Nullable TexturePicker tankTexture;
        public int tankColor;
        public float tankMaxY;

        public List<ValveRenderData> valves = new ArrayList<>();
        public MekanismRenderer.@Nullable ValveTextureGetter valveTexture;

    }
}