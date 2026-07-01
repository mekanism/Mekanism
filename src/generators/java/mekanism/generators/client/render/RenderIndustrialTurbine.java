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
    public void extractRenderState(TileEntityTurbineCasing turbine, TurbineMultiblockData multiblock, TurbineRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        state.height = multiblock.lowerVolume / ((state.length + 2) * (state.width + 2));
        if (state.height > 0) {
            ChemicalResource steam = multiblock.chemicalTank.resource();
            state.steamTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getChemicalTexture(steam));
            state.steamMaxY = ModelRenderer.getMaxY(state.height, multiblock.prevSteamScale, steam.is(MekanismAPITags.Chemicals.GASEOUS));
            state.steamColor = MekanismRenderer.getColorARGB(steam, multiblock.prevSteamScale);
            state.calculateLightCoords(turbine.getLevel(), multiblock, steam.value().lightLevel());
        }
    }

    @Override
    public void submit(TurbineRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.steamTexture != null) {
            RenderResizableCuboid.renderObject(camera.pos, poseStack, Sheets.translucentBlockItemSheet(), nodeCollector, RenderResizableCuboid.SideRender.ALL_FACES,
                  0.01F, 0.01F, 0.01F, state.length - 0.02F, state.steamMaxY, state.width - 0.02F, state.steamTexture,
                  OverlayTexture.NO_OVERLAY, state.lightCoords, state.steamColor, state.blockPos, state.renderLocation, state.length, state.width);
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.INDUSTRIAL_TURBINE;
    }

    @Override
    protected boolean shouldRender(TileEntityTurbineCasing tile, TurbineMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && !multiblock.chemicalTank.isEmpty();
    }

    public static class TurbineRenderState extends MultiblockContentsRenderState {
        public RenderResizableCuboid.@Nullable TexturePicker steamTexture;
        public float steamMaxY;
        public int steamColor;
    }
}