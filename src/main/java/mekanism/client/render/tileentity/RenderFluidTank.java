package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
public class RenderFluidTank extends MekanismTileEntityRenderer<TileEntityFluidTank> {

    private static final FluidRenderMap<Int2ObjectMap<Model3D>> cachedCenterFluids = new FluidRenderMap<>();
    private static final FluidRenderMap<Int2ObjectMap<Model3D>> cachedValveFluids = new FluidRenderMap<>();

    private static int stages = 1400;

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
        FluidStack valveFluid = tile.valveFluid;
        if (!fluid.isEmpty() && fluidScale > 0) {
            if (tile.tier == FluidTankTier.CREATIVE) {
                fluidScale = 1;
            }
            matrix.push();
            GlowInfo glowInfo = MekanismRenderer.enableGlow(fluid);
            int modelNumber;
            if (fluid.getFluid().getAttributes().isGaseous(fluid)) {
                modelNumber = stages - 1;
            } else {
                modelNumber = Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)));
            }
            MekanismRenderer.renderObject(getFluidModel(fluid, modelNumber), matrix, renderer, MekanismRenderType.renderFluidTankState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                  MekanismRenderer.getColorARGB(fluid, fluidScale));
            MekanismRenderer.disableGlow(glowInfo);
            matrix.pop();
        }

        if (!valveFluid.isEmpty() && !valveFluid.getFluid().getAttributes().isGaseous(valveFluid)) {
            matrix.push();
            GlowInfo glowInfo = MekanismRenderer.enableGlow(valveFluid);
            Model3D valveModel = getValveModel(valveFluid, Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1))));
            MekanismRenderer.renderObject(valveModel, matrix, renderer, MekanismRenderType.renderFluidTankState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                  MekanismRenderer.getColorARGB(valveFluid));
            MekanismRenderer.disableGlow(glowInfo);
            matrix.pop();
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
        BlockState state = MekanismUtils.getFlowingBlockState(fluid);
        //TODO: Check air better, given we don't have any position information
        model.baseBlock = state.isAir() ? Blocks.WATER : state.getBlock();
        MekanismRenderer.prepFlowing(model, fluid);
        if (fluid.getFluid().getAttributes().getStillTexture(fluid) != null) {
            model.minX = 0.3125 + .01;
            model.minY = 0.0625 + ((float) stage / (float) stages) * 0.875;
            model.minZ = 0.3125 + .01;

            model.maxX = 0.6875 - .01;
            model.maxY = 0.9375 - .01;
            model.maxZ = 0.6875 - .01;
        }
        if (cachedValveFluids.containsKey(fluid)) {
            cachedValveFluids.get(fluid).put(stage, model);
        } else {
            Int2ObjectMap<Model3D> map = new Int2ObjectOpenHashMap<>();
            map.put(stage, model);
            cachedValveFluids.put(fluid, map);
        }
        return model;
    }

    private Model3D getFluidModel(@Nonnull FluidStack fluid, int stage) {
        if (cachedCenterFluids.containsKey(fluid) && cachedCenterFluids.get(fluid).containsKey(stage)) {
            return cachedCenterFluids.get(fluid).get(stage);
        }
        Model3D model = new Model3D();
        BlockState state = MekanismUtils.getFlowingBlockState(fluid);
        //TODO: Check air better, given we don't have any position information
        model.baseBlock = state.isAir() ? Blocks.WATER : state.getBlock();
        model.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));
        if (fluid.getFluid().getAttributes().getStillTexture(fluid) != null) {
            model.minX = 0.125 + .01;
            model.minY = 0.0625 + .01;
            model.minZ = 0.125 + .01;

            model.maxX = 0.875 - .01;
            model.maxY = 0.0625 + ((float) stage / (float) stages) * 0.875 - .01;
            model.maxZ = 0.875 - .01;
        }
        if (cachedCenterFluids.containsKey(fluid)) {
            cachedCenterFluids.get(fluid).put(stage, model);
        } else {
            Int2ObjectMap<Model3D> map = new Int2ObjectOpenHashMap<>();
            map.put(stage, model);
            cachedCenterFluids.put(fluid, map);
        }
        return model;
    }
}