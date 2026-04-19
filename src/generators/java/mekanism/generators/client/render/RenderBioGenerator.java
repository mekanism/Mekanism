package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.util.EnumUtils;
import mekanism.generators.client.render.RenderBioGenerator.BioGeneratorRenderState;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderBioGenerator extends MekanismTileEntityRenderer<TileEntityBioGenerator, BioGeneratorRenderState> {

    private static final Map<Direction, Int2ObjectMap<Model3D>> fuelModels = new EnumMap<>(Direction.class);
    private static final int stages = 40;

    public static void resetCachedModels() {
        fuelModels.clear();
    }

    public RenderBioGenerator(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BioGeneratorRenderState createRenderState() {
        return new BioGeneratorRenderState();
    }

    @Override
    public void extractRenderState(TileEntityBioGenerator generator, BioGeneratorRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(generator, state, partialTick, cameraPosition, breakProgress);
        FluidStack fluid = generator.bioFuelTank.getFluid();
        float fluidScale = fluid.getAmount() / (float) generator.bioFuelTank.getCapacity();
        state.model = getModel(fluid, generator.getDirection(), fluidScale);
        state.tint = MekanismRenderer.getColorARGB(fluid, fluidScale);
    }

    @Override
    public void submit(BioGeneratorRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.model != null) {
            //TODO - 26.1: Do we want to use the block light? (Also check other full bright usages and see if they should be switched over)
            MekanismRenderer.renderObject(state.model, poseStack, Sheets.translucentCullBlockSheet(), state.tint, LightCoordsUtil.FULL_BRIGHT,
                  OverlayTexture.NO_OVERLAY, FaceDisplay.FRONT, camera.pos, state.blockPos);
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.BIO_GENERATOR;
    }

    @Override
    public boolean shouldRender(TileEntityBioGenerator tile, Vec3 camera) {
        return !tile.bioFuelTank.isEmpty() && super.shouldRender(tile, camera);
    }

    private Model3D getModel(FluidStack fluid, Direction side, float fluidScale) {
        Int2ObjectMap<Model3D> modelMap = fuelModels.computeIfAbsent(side, s -> new Int2ObjectOpenHashMap<>());
        int stage = ModelRenderer.getStage(fluid, stages, fluidScale);
        Model3D model = modelMap.get(stage);
        if (model == null) {
            model = new Model3D()
                  .setTexture(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL))
                  .yBounds(0.4385F, 0.4385F + 0.4375F * (stage / (float) stages));
            Direction opposite = side.getOpposite();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                model.setSideRender(direction, direction == Direction.UP || direction == opposite);
            }
            switch (side) {
                case NORTH -> model
                      .xBounds(0.188F, 0.821F)
                      .zBounds(0.499F, 0.875F);
                case SOUTH -> model
                      .xBounds(0.188F, 0.821F)
                      .zBounds(0.125F, 0.499F);
                case WEST -> model
                      .xBounds(0.499F, 0.875F)
                      .zBounds(0.187F, 0.821F);
                case EAST -> model
                      .xBounds(0.125F, 0.499F)
                      .zBounds(0.186F, 0.821F);
            }
            modelMap.put(stage, model);
        }
        return model;
    }

    public static class BioGeneratorRenderState extends BlockEntityRenderState {

        @Nullable
        public Model3D model;
        public int tint = 0xFFFFFFFF;
    }
}