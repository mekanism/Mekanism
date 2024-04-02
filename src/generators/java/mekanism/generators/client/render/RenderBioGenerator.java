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
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

@NothingNullByDefault
public class RenderBioGenerator extends MekanismTileEntityRenderer<TileEntityBioGenerator> {

    private static final Map<Direction, Int2ObjectMap<Model3D>> fuelModels = new EnumMap<>(Direction.class);
    private static final int stages = 40;

    public static void resetCachedModels() {
        fuelModels.clear();
    }

    public RenderBioGenerator(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityBioGenerator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        matrix.pushPose();
        FluidStack fluid = tile.bioFuelTank.getFluid();
        float fluidScale = fluid.getAmount() / (float) tile.bioFuelTank.getCapacity();
        MekanismRenderer.renderObject(getModel(fluid, tile.getDirection(), fluidScale), matrix,
              renderer.getBuffer(Sheets.translucentCullBlockSheet()), MekanismRenderer.getColorARGB(fluid, fluidScale), LightTexture.FULL_BRIGHT, overlayLight,
              FaceDisplay.FRONT, getCamera(), tile.getBlockPos());
        matrix.popPose();
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
}