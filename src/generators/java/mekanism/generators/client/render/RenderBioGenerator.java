package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
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
        if (!tile.bioFuelTank.isEmpty()) {
            matrix.pushPose();
            FluidStack fluid = tile.bioFuelTank.getFluid();
            float fluidScale = fluid.getAmount() / (float) tile.bioFuelTank.getCapacity();
            int modelNumber = ModelRenderer.getStage(fluid, stages, fluidScale);
            MekanismRenderer.renderObject(getModel(tile.getDirection(), modelNumber), matrix,
                  renderer.getBuffer(Sheets.translucentCullBlockSheet()), MekanismRenderer.getColorARGB(fluid, fluidScale), MekanismRenderer.FULL_LIGHT, overlayLight,
                  FaceDisplay.FRONT);
            matrix.popPose();
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.BIO_GENERATOR;
    }

    @SuppressWarnings("incomplete-switch")
    private Model3D getModel(Direction side, int stage) {
        if (fuelModels.containsKey(side) && fuelModels.get(side).containsKey(stage)) {
            return fuelModels.get(side).get(stage);
        }
        Model3D model = new Model3D();
        model.setTexture(MekanismRenderer.getFluidTexture(GeneratorsFluids.BIOETHANOL.getFluidStack(1), FluidType.STILL));
        switch (side) {
            case NORTH -> {
                model.minZ = 0.499F;
                model.maxZ = 0.875F;
                model.minX = 0.188F;
                model.maxX = 0.821F;
            }
            case SOUTH -> {
                model.minZ = 0.125F;
                model.maxZ = 0.499F;
                model.minX = 0.188F;
                model.maxX = 0.821F;
            }
            case WEST -> {
                model.minX = 0.499F;
                model.maxX = 0.875F;
                model.minZ = 0.187F;
                model.maxZ = 0.821F;
            }
            case EAST -> {
                model.minX = 0.125F;
                model.maxX = 0.499F;
                model.minZ = 0.186F;
                model.maxZ = 0.821F;
            }
        }
        model.minY = 0.4385F;//0.4375 + 0.001; - prevent z fighting at low fuel levels
        model.maxY = 0.4385F + 0.4375F * (stage / (float) stages);//0.4375 + 0.001 + 0.4375 * (stage / (float) stages);
        fuelModels.computeIfAbsent(side, s -> new Int2ObjectOpenHashMap<>()).putIfAbsent(stage, model);
        return model;
    }
}