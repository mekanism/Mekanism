package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.data.ChemicalRenderData.GasRenderData;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderFissionReactor extends MekanismTileEntityRenderer<TileEntityFissionReactorCasing> {

    private static final Map<RenderData, Model3D> cachedHeatedCoolantModels = new Object2ObjectOpenHashMap<>();
    private static Model3D glowModel;

    public static void resetCachedModels() {
        cachedHeatedCoolantModels.clear();
        glowModel = null;
    }

    public RenderFissionReactor(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityFissionReactorCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        if (tile.isMaster) {
            FissionReactorMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed() && multiblock.renderLocation != null) {
                BlockPos pos = tile.getPos();
                IVertexBuilder buffer = renderer.getBuffer(Atlases.getTranslucentCullBlockType());
                if (multiblock.isBurning()) {
                    //TODO - 10.1: Convert the glow model and stuff to being part of the baked model and using model data
                    // as I am fairly sure that should give a decent boost to performance
                    if (glowModel == null) {
                        glowModel = new Model3D();
                        glowModel.minX = 0.05F;
                        glowModel.minY = 0.01F;
                        glowModel.minZ = 0.05F;
                        glowModel.maxX = 0.95F;
                        glowModel.maxY = 0.99F;
                        glowModel.maxZ = 0.95F;
                        glowModel.setTexture(MekanismRenderer.whiteIcon);
                    }
                    for (FormedAssembly assembly : multiblock.assemblies) {
                        matrix.push();
                        matrix.translate(assembly.getPos().getX() - pos.getX(), assembly.getPos().getY() - pos.getY(), assembly.getPos().getZ() - pos.getZ());
                        matrix.scale(1, assembly.getHeight(), 1);
                        int argb = MekanismRenderer.getColorARGB(0.466F, 0.882F, 0.929F, 0.6F);
                        MekanismRenderer.renderObject(glowModel, matrix, buffer, argb, MekanismRenderer.FULL_LIGHT, overlayLight, FaceDisplay.FRONT);
                        matrix.pop();
                    }
                }
                if (!multiblock.fluidCoolantTank.isEmpty()) {
                    int height = multiblock.height() - 2;
                    if (height >= 1) {
                        FluidRenderData data = new FluidRenderData(multiblock.fluidCoolantTank.getFluid());
                        data.location = multiblock.renderLocation;
                        data.height = height;
                        data.length = multiblock.length();
                        data.width = multiblock.width();
                        int glow = data.calculateGlowLight(MekanismRenderer.FULL_SKY_LIGHT);
                        matrix.push();
                        matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                        Model3D model = ModelRenderer.getModel(data, multiblock.prevCoolantScale);
                        MekanismRenderer.renderObject(model, matrix, buffer, data.getColorARGB(multiblock.prevCoolantScale), glow, overlayLight, getFaceDisplay(data, model));
                        matrix.pop();
                        MekanismRenderer.renderValves(matrix, buffer, multiblock.valves, data, pos, glow, overlayLight, isInsideMultiblock(data));
                    }
                }
                if (!multiblock.heatedCoolantTank.isEmpty()) {
                    int height = multiblock.height() - 2;
                    if (height >= 1) {
                        GasRenderData data = new GasRenderData(multiblock.heatedCoolantTank.getStack());
                        data.location = multiblock.renderLocation;
                        data.height = height;
                        data.length = multiblock.length();
                        data.width = multiblock.width();
                        int glow = data.calculateGlowLight(MekanismRenderer.FULL_SKY_LIGHT);
                        Model3D gasModel;
                        if (cachedHeatedCoolantModels.containsKey(data)) {
                            gasModel = cachedHeatedCoolantModels.get(data);
                        } else {
                            //Create a slightly shrunken version of the model if it is missing to prevent z-fighting
                            gasModel = ModelRenderer.getModel(data, 1).copy();
                            gasModel.minX += 0.01F;
                            gasModel.minY += 0.01F;
                            gasModel.minZ += 0.01F;
                            gasModel.maxX -= 0.01F;
                            gasModel.maxY -= 0.01F;
                            gasModel.maxZ -= 0.01F;
                            cachedHeatedCoolantModels.put(data, gasModel);
                        }
                        matrix.push();
                        matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                        MekanismRenderer.renderObject(gasModel, matrix, buffer, data.getColorARGB(multiblock.prevHeatedCoolantScale), glow, overlayLight,
                              getFaceDisplay(data, gasModel));
                        matrix.pop();
                    }
                }
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FISSION_REACTOR;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityFissionReactorCasing tile) {
        if (tile.isMaster) {
            FissionReactorMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && multiblock.renderLocation != null;
        }
        return false;
    }
}