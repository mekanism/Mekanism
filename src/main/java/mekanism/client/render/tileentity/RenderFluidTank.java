package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackLinkedSet;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RenderFluidTank extends MekanismTileEntityRenderer<TileEntityFluidTank> {

    private static final Map<FluidStack, Int2ObjectMap<Model3D>> cachedCenterFluids = new Object2ObjectOpenCustomHashMap<>(FluidStackLinkedSet.TYPE_AND_COMPONENTS);
    private static final Map<FluidStack, Int2ObjectMap<Model3D>> cachedValveFluids = new Object2ObjectOpenCustomHashMap<>(FluidStackLinkedSet.TYPE_AND_COMPONENTS);

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
        float fluidScale = fluid.isEmpty() ? 0 : tile.prevScale;
        VertexConsumer buffer = null;
        if (fluidScale > 0) {
            buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
            MekanismRenderer.renderObject(getFluidModel(fluid, fluidScale), matrix, buffer, MekanismRenderer.getColorARGB(fluid, fluidScale),
                  MekanismRenderer.calculateGlowLight(light, fluid), overlayLight, FaceDisplay.FRONT, getCamera(), tile.getBlockPos());
        }
        if (!tile.valveFluid.isEmpty() && !MekanismUtils.lighterThanAirGas(tile.valveFluid)) {
            if (buffer == null) {
                buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
            }
            MekanismRenderer.renderObject(getValveModel(tile.valveFluid, fluidScale), matrix, buffer,
                  MekanismRenderer.getColorARGB(tile.valveFluid), MekanismRenderer.calculateGlowLight(light, tile.valveFluid), overlayLight, FaceDisplay.FRONT,
                  getCamera(), tile.getBlockPos());
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.FLUID_TANK;
    }

    private Model3D getValveModel(@NotNull FluidStack fluid, float fluidScale) {
        Int2ObjectMap<Model3D> modelMap = cachedValveFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>());
        int stage = Math.min(stages - 1, (int) (fluidScale * (stages - 1)));
        Model3D model = modelMap.get(stage);
        if (model == null) {
            model = new Model3D()
                  .setSideRender(side -> side.getAxis().isHorizontal())
                  .prepFlowing(fluid)
                  .xBounds(0.3225F, 0.6775F)
                  .yBounds(0.12375F + 0.7525F * (stage / (float) stages), 0.87625F)
                  .zBounds(0.3225F, 0.6775F);
            modelMap.put(stage, model);
        }
        return model;
    }

    public static Model3D getFluidModel(@NotNull FluidStack fluid, float fluidScale) {
        Int2ObjectMap<Model3D> modelMap = cachedCenterFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>());
        int stage = ModelRenderer.getStage(fluid, stages, fluidScale);
        Model3D model = modelMap.get(stage);
        if (model == null) {
            model = new Model3D()
                  .setTexture(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL))
                  .setSideRender(Direction.DOWN, false)
                  .setSideRender(Direction.UP, stage < stages)
                  .xBounds(0.135F, 0.865F)
                  .yBounds(0.12375F, 0.124F + 0.75225F * (stage / (float) stages))
                  .zBounds(0.135F, 0.865F);
            modelMap.put(stage, model);
        }
        return model;
    }
}