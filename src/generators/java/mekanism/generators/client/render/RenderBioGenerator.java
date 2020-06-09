package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelBioGenerator;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class RenderBioGenerator extends MekanismTileEntityRenderer<TileEntityBioGenerator> {

    private static final Map<Direction, Int2ObjectMap<Model3D>> energyDisplays = new EnumMap<>(Direction.class);
    private static final int stages = 40;
    private final ModelBioGenerator model = new ModelBioGenerator();

    public static void resetCachedModels() {
        energyDisplays.clear();
    }

    public RenderBioGenerator(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityBioGenerator tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (!tile.bioFuelTank.isEmpty()) {
            matrix.push();
            FluidStack fluid = tile.bioFuelTank.getFluid();
            float fluidScale = fluid.getAmount() / (float) tile.bioFuelTank.getCapacity();
            //TODO: FIXME, you can see through the back. Might have to make the main "model" into json and then just render the fluid as a TER
            // Note: This issue is much less noticeable now that we have a proper fluid that we are using for biofuel
            MekanismRenderer.renderObject(getModel(tile.getDirection(), (int) (fluidScale * (stages - 1))), matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()),
                  MekanismRenderer.getColorARGB(fluid, fluidScale), MekanismRenderer.calculateGlowLight(light, fluid));
            matrix.pop();
        }
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 180, 0, 270, 90);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        model.render(matrix, renderer, light, overlayLight, false);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.BIO_GENERATOR;
    }

    @SuppressWarnings("incomplete-switch")
    private Model3D getModel(Direction side, int stage) {
        if (energyDisplays.containsKey(side) && energyDisplays.get(side).containsKey(stage)) {
            return energyDisplays.get(side).get(stage);
        }
        Model3D model = new Model3D();
        model.setTexture(MekanismRenderer.getFluidTexture(GeneratorsFluids.BIOETHANOL.getFluidStack(1), FluidType.STILL));
        switch (side) {
            case NORTH:
                model.minZ = 0.5;
                model.maxZ = 0.875;

                model.minX = 0.1875;
                model.maxX = 0.8215;
                break;
            case SOUTH:
                model.minZ = 0.125;
                model.maxZ = 0.5;

                model.minX = 0.1875;
                model.maxX = 0.8215;
                break;
            case WEST:
                model.minX = 0.5;
                model.maxX = 0.875;

                model.minZ = 0.1875;
                model.maxZ = 0.8215;
                break;
            case EAST:
                model.minX = 0.125;
                model.maxX = 0.5;

                model.minZ = 0.1875;
                model.maxZ = 0.8215;
                break;
        }
        model.minY = 0.4375 + 0.001;  //prevent z fighting at low fuel levels
        model.maxY = 0.4375 + ((float) stage / stages) * 0.4375 + 0.001;
        energyDisplays.computeIfAbsent(side, s -> new Int2ObjectOpenHashMap<>()).putIfAbsent(stage, model);
        return model;
    }
}