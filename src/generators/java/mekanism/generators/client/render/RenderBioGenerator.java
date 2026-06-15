package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.SideRender;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.util.MekanismUtils;
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
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class RenderBioGenerator extends MekanismTileEntityRenderer<TileEntityBioGenerator, BioGeneratorRenderState> {

    private static final int stages = 40;
    public static final float MODEL_MIN_Y_PAD = 0.4385F;
    public static final float MODEL_Y_STAGE_FRACTION = 0.4375F;
    //todo: figure out better names for these? Based on their first usages. Some are 0.001 different, is that important?
    public static final float MODEL_X_MIN_NORTH_SOUTH = 0.188F;
    public static final float MODEL_X_MAX_NORTH_SOUTH = 0.821F;
    public static final float MODEL_Z_MIN_NORTH = 0.499F;
    public static final float MODEL_Z_MAX_NORTH = 0.875F;
    public static final float MODEL_Z_MIN_SOUTH = 0.125F;
    public static final float MODEL_Z_MIN_WEST = 0.187F;
    public static final float MODEL_Z_MIN_EAST = 0.186F;

    public RenderBioGenerator(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BioGeneratorRenderState createRenderState() {
        return new BioGeneratorRenderState();
    }

    @Override
    public void extractRenderState(TileEntityBioGenerator generator, BioGeneratorRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(generator, state, partialTick, cameraPosition, breakProgress);
        FluidResource fluid = generator.bioFuelTank.resource();
        float fluidScale = generator.bioFuelTank.amountAsLong() / (float) generator.bioFuelTank.capacityAsLong(fluid);
        state.maxY = MODEL_MIN_Y_PAD + MODEL_Y_STAGE_FRACTION * getFluidStagePercent(fluidScale, MekanismUtils.lighterThanAirGas(fluid));
        switch (generator.getDirection()) {
            case NORTH -> {
                state.minX = MODEL_X_MIN_NORTH_SOUTH;
                state.maxX = MODEL_X_MAX_NORTH_SOUTH;
                state.minZ = MODEL_Z_MIN_NORTH;
                state.maxZ = MODEL_Z_MAX_NORTH;
            }
            case SOUTH -> {
                state.minX = MODEL_X_MIN_NORTH_SOUTH;
                state.maxX = MODEL_X_MAX_NORTH_SOUTH;
                state.minZ = MODEL_Z_MIN_SOUTH;
                state.maxZ = MODEL_Z_MIN_NORTH;
            }
            case WEST -> {
                state.minX = MODEL_Z_MIN_NORTH;
                state.maxX = MODEL_Z_MAX_NORTH;
                state.minZ = MODEL_Z_MIN_WEST;
                state.maxZ = MODEL_X_MAX_NORTH_SOUTH;
            }
            case EAST -> {
                state.minX = MODEL_Z_MIN_SOUTH;
                state.maxX = MODEL_Z_MIN_NORTH;
                state.minZ = MODEL_Z_MIN_EAST;
                state.maxZ = MODEL_X_MAX_NORTH_SOUTH;
            }
        }
        state.fluidTexture = MekanismRenderer.getSinglePicker(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL));
        state.tint = MekanismRenderer.getColorARGB(fluid, fluidScale);
        //noinspection MagicConstant
        state.renderCheck = (byte) (SideRender.FACE_UP | SideRender.of(generator.getDirection().getOpposite()));
    }

    private static float getFluidStagePercent(float fluidScale, boolean gaseous) {
        return ModelRenderer.getStage(gaseous, stages, fluidScale) / (float) stages;
    }

    @Override
    public void submit(BioGeneratorRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.fluidTexture != null) {
            //TODO - 26.1: Do we want to use the block light? (Also check other full bright usages and see if they should be switched over)
            //TODO - 26.2: Validate this render sheet
            RenderResizableCuboid.renderCube(state.renderCheck, state.minX, MODEL_MIN_Y_PAD, state.minZ, state.maxX, state.maxY, state.maxZ, poseStack, Sheets.translucentBlockItemSheet(), nodeCollector, state.tint, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos), state.fluidTexture);
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

    public static class BioGeneratorRenderState extends BlockEntityRenderState {

        public float minX, minZ;
        public float maxX, maxY, maxZ;
        public int tint = CommonColors.WHITE;
        public RenderResizableCuboid.@Nullable TexturePicker fluidTexture;
        @SideRender.SideRenderFlags
        public byte renderCheck = SideRender.ALL_FACES;

    }
}