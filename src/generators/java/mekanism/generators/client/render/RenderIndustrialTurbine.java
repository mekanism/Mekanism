package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalResource;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.MultiblockContentsRenderState;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.generators.client.render.RenderIndustrialTurbine.TurbineRenderState;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderIndustrialTurbine extends MultiblockTileEntityRenderer<TurbineMultiblockData, TileEntityTurbineCasing, TurbineRenderState> {

    public RenderIndustrialTurbine(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TurbineRenderState createRenderState() {
        return new TurbineRenderState();
    }

    @Override
    public void extractRenderState(TileEntityTurbineCasing turbine, TurbineRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(turbine, state, partialTick, cameraPosition, breakProgress);
        TurbineMultiblockData multiblock = turbine.getMultiblock();
        state.gather(multiblock);
        state.steamTexture = null;
        ChemicalResource steam = multiblock.chemicalTank.resource();
        if (!steam.isEmpty() && multiblock.length() > 0) {
            int height = multiblock.lowerVolume / (multiblock.length() * multiblock.width());
            state.height = height;
            if (height > 0) {
                state.steamTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(steam));
                state.steamMaxY = ModelRenderer.getMaxY(state.height, multiblock.prevSteamScale, steam.is(MekanismAPITags.Chemicals.GASEOUS));
                state.steamColor = MekanismRenderer.getColorARGB(steam, multiblock.prevSteamScale);
            }
        }
    }

    @Override
    public void submit(TurbineRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.steamTexture != null) {
            RenderResizableCuboid.renderObject(camera.pos, poseStack, Sheets.translucentBlockSheet(), nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES,
                  0.01F, 0.01F, 0.01F, state.length - 0.02F, state.steamMaxY, state.width - 0.02F, state.steamTexture,
                  OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_SKY, state.steamColor, state.blockPos, state.renderLocation, state.length, state.width);
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.INDUSTRIAL_TURBINE;
    }

    @Override
    protected boolean shouldRender(TileEntityTurbineCasing tile, TurbineMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && multiblock.complex != null;
    }

    public static class TurbineRenderState extends MultiblockContentsRenderState {
        public RenderResizableCuboid.@Nullable TexturePicker steamTexture;
        public float steamMaxY;
        public int steamColor;
    }
}