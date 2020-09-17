package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.GasRenderData;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fission.FissionReactorValidator.FormedAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderFissionReactor extends MekanismTileEntityRenderer<TileEntityFissionReactorCasing> {

    private static Model3D glowModel;

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
                    if (glowModel == null) {
                        glowModel = new Model3D();
                        glowModel.minX = 0.05;
                        glowModel.minY = 0.01;
                        glowModel.minZ = 0.05;
                        glowModel.maxX = 0.95;
                        glowModel.maxY = 0.99;
                        glowModel.maxZ = 0.95;
                        glowModel.setTexture(MekanismRenderer.whiteIcon);
                    }
                    for (FormedAssembly assembly : multiblock.assemblies) {
                        matrix.push();
                        matrix.translate(assembly.getPos().getX() - pos.getX(), assembly.getPos().getY() - pos.getY(), assembly.getPos().getZ() - pos.getZ());
                        matrix.scale(1, assembly.getHeight(), 1);
                        int argb = MekanismRenderer.getColorARGB(0.466F, 0.882F, 0.929F, 0.6F);
                        MekanismRenderer.renderObject(glowModel, matrix, buffer, argb, MekanismRenderer.FULL_LIGHT, overlayLight);
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
                        int glow = data.calculateGlowLight(LightTexture.packLight(0, 15));
                        matrix.push();
                        matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                        MekanismRenderer.renderObject(ModelRenderer.getModel(data, multiblock.prevCoolantScale), matrix, buffer,
                              data.getColorARGB(multiblock.prevCoolantScale), glow, overlayLight);
                        matrix.pop();
                        MekanismRenderer.renderValves(matrix, buffer, multiblock.valves, data, pos, glow, overlayLight);
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
                        int glow = data.calculateGlowLight(LightTexture.packLight(0, 15));
                        matrix.push();
                        matrix.scale(0.998F, 0.998F, 0.998F);
                        matrix.translate(data.location.getX() - pos.getX() + 0.001, data.location.getY() - pos.getY() + 0.001, data.location.getZ() - pos.getZ() + 0.001);
                        Model3D gasModel = ModelRenderer.getModel(data, 1);
                        MekanismRenderer.renderObject(gasModel, matrix, buffer, data.getColorARGB(multiblock.prevHeatedCoolantScale), glow, overlayLight);
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