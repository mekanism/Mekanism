package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.LazyModel;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.util.Lazy;

@NothingNullByDefault
public class RenderFissionReactor extends MultiblockTileEntityRenderer<FissionReactorMultiblockData, TileEntityFissionReactorCasing> {

    private static final Map<RenderData, Model3D> cachedHeatedCoolantModels = new Object2ObjectOpenHashMap<>();
    private static final int GLOW_ARGB = MekanismRenderer.getColorARGB(0.466F, 0.882F, 0.929F, 0.6F);
    //TODO: Replace using a model here for the glow with using FuelAssemblyBakedModel as it should provide a performance boost
    // The issue and reason it doesn't use it yet is because rendering the coolant hides the FuelAssemblyBakedModel due to
    // transparency sort ordering
    private static final MekanismRenderer.LazyModel glowModel = new LazyModel(() -> new Model3D()
          .setTexture(MekanismRenderer.whiteIcon)
          .xBounds(0.05F, 0.95F)
          .yBounds(0.01F, 0.99F)
          .zBounds(0.05F, 0.95F)
          .setSideRender(direction -> direction.getAxis().isHorizontal())
    );

    public static void resetCachedModels() {
        cachedHeatedCoolantModels.clear();
        glowModel.reset();
    }

    public RenderFissionReactor(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityFissionReactorCasing tile, FissionReactorMultiblockData multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, ProfilerFiller profiler) {
        BlockPos pos = tile.getBlockPos();
        Lazy<VertexConsumer> buffer = Lazy.of(() -> renderer.getBuffer(Sheets.translucentCullBlockSheet()));
        if (multiblock.isBurning()) {
            profiler.push(GeneratorsProfilerConstants.FISSION_FUEL_ASSEMBLY);
            Model3D model = glowModel.get();
            Camera camera = getCamera();
            for (FormedAssembly assembly : multiblock.assemblies) {
                BlockPos assemblyPos = assembly.pos();
                matrix.pushPose();
                matrix.translate(assemblyPos.getX() - pos.getX(), assemblyPos.getY() - pos.getY(), assemblyPos.getZ() - pos.getZ());
                //Add a bit of extra distance so that it includes the lower part of the control rod
                matrix.scale(1, assembly.height() + 0.625F, 1);
                MekanismRenderer.renderObject(model, matrix, buffer.get(), GLOW_ARGB, LightTexture.FULL_BRIGHT, overlayLight, FaceDisplay.FRONT, camera, assemblyPos);
                matrix.popPose();
            }
            profiler.pop();
        }
        if (!multiblock.fluidCoolantTank.isEmpty()) {
            FluidRenderData data = RenderData.Builder.create(multiblock.fluidCoolantTank.getFluid()).of(multiblock).build();
            renderObject(data, multiblock.valves, pos, matrix, buffer.get(), overlayLight, multiblock.prevCoolantScale);
        }
        if (!multiblock.heatedCoolantTank.isEmpty()) {
            RenderData data = RenderData.Builder.create(multiblock.heatedCoolantTank.getStack()).of(multiblock).build();
            //Create a slightly shrunken version of the model if it is missing to prevent z-fighting
            Model3D gasModel = cachedHeatedCoolantModels.computeIfAbsent(data, d -> ModelRenderer.getModel(d, 1).copy().shrink(0.01F));
            renderObject(data, pos, gasModel, matrix, buffer.get(), overlayLight, multiblock.prevHeatedCoolantScale);
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FISSION_REACTOR;
    }
}