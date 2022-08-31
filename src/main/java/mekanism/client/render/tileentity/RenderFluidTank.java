package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RenderFluidTank extends MekanismTileEntityRenderer<TileEntityFluidTank> {

    private static final FluidRenderMap<Int2ObjectMap<Model3D>> cachedCenterFluids = new FluidRenderMap<>();
    private static final FluidRenderMap<Int2ObjectMap<Model3D>> cachedValveFluids = new FluidRenderMap<>();

    private static final int stages = 1_400;

    public RenderFluidTank(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static void resetCachedModels() {
        cachedCenterFluids.clear();
        cachedValveFluids.clear();
    }

    @Override
    protected void render(TileEntityFluidTank tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        FluidStack fluid = tile.fluidTank.getFluid();
        float fluidScale = tile.prevScale;
        Lazy<VertexConsumer> buffer = Lazy.of(() -> renderer.getBuffer(Sheets.translucentCullBlockSheet()));
        if (!fluid.isEmpty() && fluidScale > 0) {
            MekanismRenderer.renderObject(getFluidModel(fluid, fluidScale), matrix, buffer.get(), MekanismRenderer.getColorARGB(fluid, fluidScale),
                  MekanismRenderer.calculateGlowLight(light, fluid), overlayLight, FaceDisplay.FRONT, getCamera(), tile.getBlockPos());
        }
        if (!tile.valveFluid.isEmpty() && !MekanismUtils.lighterThanAirGas(tile.valveFluid)) {
            MekanismRenderer.renderObject(getValveModel(tile.valveFluid, fluidScale), matrix, buffer.get(),
                  MekanismRenderer.getColorARGB(tile.valveFluid), MekanismRenderer.calculateGlowLight(light, tile.valveFluid), overlayLight, FaceDisplay.FRONT,
                  getCamera(), tile.getBlockPos());
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.FLUID_TANK;
    }

    private Model3D getValveModel(@NotNull FluidStack fluid, float fluidScale) {
        return cachedValveFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>())
              .computeIfAbsent(Math.min(stages - 1, (int) (fluidScale * (stages - 1))), stage -> new Model3D()
                    .setSideRender(side -> side.getAxis().isHorizontal())
                    .prepFlowing(fluid)
                    .xBounds(0.3225F, 0.6775F)
                    .yBounds(0.0625F + 0.875F * (stage / (float) stages), 0.9375F)
                    .zBounds(0.3225F, 0.6775F)
              );
    }

    public static Model3D getFluidModel(@NotNull FluidStack fluid, float fluidScale) {
        return cachedCenterFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>())
              .computeIfAbsent(ModelRenderer.getStage(fluid, stages, fluidScale), stage -> new Model3D()
                    .setTexture(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL))
                    .setSideRender(Direction.DOWN, false)
                    .setSideRender(Direction.UP, stage < stages)
                    .xBounds(0.135F, 0.865F)
                    .yBounds(0.0625F, 0.0625F + 0.875F * (stage / (float) stages))
                    .zBounds(0.135F, 0.865F)
              );
    }
}