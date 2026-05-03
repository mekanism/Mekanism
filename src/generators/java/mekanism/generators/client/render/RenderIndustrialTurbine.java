package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.generators.client.render.RenderIndustrialTurbine.TurbineRenderState;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
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
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(turbine, state, partialTick, cameraPosition, breakProgress);
        TurbineMultiblockData multiblock = turbine.getMultiblock();
        state.steamScale = multiblock.prevSteamScale;
        state.steamData = null;
        if (!multiblock.chemicalTank.isEmpty() && multiblock.length() > 0) {
            int height = multiblock.lowerVolume / (multiblock.length() * multiblock.width());
            if (height > 0) {
                ChemicalStack chemicalStack = multiblock.chemicalTank.getStack();
                state.steamData = RenderData.Builder.create(chemicalStack)
                      .of(multiblock)
                      .height(height)
                      .build();
                state.steamTexture = MekanismRenderer.getChemicalTexture(chemicalStack);
            }
        }
    }

    @Override
    public void submit(TurbineRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.steamData != null) {
            MekanismRenderer.Model3D model = ModelRenderer.getModel(state.steamData, state.steamScale);
            RenderResizableCuboid.renderObject(camera.pos, state.steamData, state.blockPos, model, poseStack, Sheets.translucentBlockSheet(), nodeCollector, OverlayTexture.NO_OVERLAY, state.steamScale, state);
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

    public static class TurbineRenderState extends BlockEntityRenderState implements Function<Direction, TextureAtlasSprite> {

        @Nullable
        public RenderData steamData;
        public float steamScale;
        @Nullable
        public TextureAtlasSprite steamTexture;

        @Override
        public TextureAtlasSprite apply(Direction direction) {
            return steamTexture;
        }
    }
}