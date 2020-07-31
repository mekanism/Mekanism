package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class RenderFluidTank extends MekanismTileEntityRenderer<TileEntityFluidTank> {

    private static final FluidRenderMap<Int2ObjectMap<Model3D>> cachedCenterFluids = new FluidRenderMap<>();
    private static final FluidRenderMap<Int2ObjectMap<Model3D>> cachedValveFluids = new FluidRenderMap<>();

    private static final int stages = 1_400;

    public RenderFluidTank(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    public static void resetCachedModels() {
        cachedCenterFluids.clear();
        cachedValveFluids.clear();
    }

    @Override
    protected void render(TileEntityFluidTank tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        FluidStack fluid = tile.fluidTank.getFluid();
        float fluidScale = tile.prevScale;
        IVertexBuilder buffer = null;
        if (!fluid.isEmpty() && fluidScale > 0) {
            int modelNumber;
            if (fluid.getFluid().getAttributes().isGaseous(fluid)) {
                modelNumber = stages - 1;
            } else {
                modelNumber = Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)));
            }
            buffer = renderer.getBuffer(Atlases.getTranslucentCullBlockType());
            MekanismRenderer.renderObject(getFluidModel(fluid, modelNumber), matrix, buffer, MekanismRenderer.getColorARGB(fluid, fluidScale),
                  MekanismRenderer.calculateGlowLight(light, fluid), overlayLight);
        }
        if (!tile.valveFluid.isEmpty() && !tile.valveFluid.getFluid().getAttributes().isGaseous(tile.valveFluid)) {
            if (buffer == null) {
                buffer = renderer.getBuffer(Atlases.getTranslucentCullBlockType());
            }
            MekanismRenderer.renderObject(getValveModel(tile.valveFluid, Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)))), matrix, buffer,
                  MekanismRenderer.getColorARGB(tile.valveFluid), MekanismRenderer.calculateGlowLight(light, tile.valveFluid), overlayLight);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.FLUID_TANK;
    }

    private Model3D getValveModel(@Nonnull FluidStack fluid, int stage) {
        if (cachedValveFluids.containsKey(fluid) && cachedValveFluids.get(fluid).containsKey(stage)) {
            return cachedValveFluids.get(fluid).get(stage);
        }
        Model3D model = new Model3D();
        MekanismRenderer.prepFlowing(model, fluid);
        if (fluid.getFluid().getAttributes().getStillTexture(fluid) != null) {
            model.minX = 0.3125 + .01;
            model.minY = 0.0625 + ((float) stage / (float) stages) * 0.875;
            model.minZ = 0.3125 + .01;

            model.maxX = 0.6875 - .01;
            model.maxY = 0.9375 - .01;
            model.maxZ = 0.6875 - .01;
        }
        cachedValveFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>()).put(stage, model);
        return model;
    }

    private Model3D getFluidModel(@Nonnull FluidStack fluid, int stage) {
        if (cachedCenterFluids.containsKey(fluid) && cachedCenterFluids.get(fluid).containsKey(stage)) {
            return cachedCenterFluids.get(fluid).get(stage);
        }
        Model3D model = new Model3D();
        model.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));
        if (fluid.getFluid().getAttributes().getStillTexture(fluid) != null) {
            model.minX = 0.125 + .01;
            model.minY = 0.0625 + .01;
            model.minZ = 0.125 + .01;

            model.maxX = 0.875 - .01;
            model.maxY = 0.0625 + ((float) stage / (float) stages) * 0.875 - .01;
            model.maxZ = 0.875 - .01;
        }
        cachedCenterFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>()).put(stage, model);
        return model;
    }
}