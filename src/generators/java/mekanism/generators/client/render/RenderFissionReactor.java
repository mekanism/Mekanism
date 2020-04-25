package mekanism.generators.client.render;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.GasRenderData;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderFissionReactor extends MekanismTileEntityRenderer<TileEntityFissionReactorCasing> {

    public RenderFissionReactor(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityFissionReactorCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.renderLocation != null) {
            BlockPos pos = tile.getPos();
            IVertexBuilder buffer = null;
            if (!tile.structure.fluidCoolantTank.isEmpty()) {
                FluidRenderData data = new FluidRenderData();
                data.height = tile.structure.volHeight - 2;
                if (data.height >= 1) {
                    data.location = tile.structure.renderLocation;
                    data.length = tile.structure.volLength;
                    data.width = tile.structure.volWidth;
                    data.fluidType = tile.structure.fluidCoolantTank.getFluid();
                    int glow = data.calculateGlowLight(light);
                    matrix.push();
                    matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
                    MekanismRenderer.renderObject(ModelRenderer.getModel(data, tile.prevCoolantScale), matrix, buffer, data.getColorARGB(tile.prevCoolantScale), glow);
                    matrix.pop();

                    MekanismRenderer.renderValves(matrix, buffer, tile.structure.valves, data, pos, glow);
                }
            } else if (!tile.structure.gasCoolantTank.isEmpty()) {
                GasRenderData data = new GasRenderData();
                data.height = tile.structure.volHeight - 2;
                if (data.height >= 1) {
                    data.location = tile.structure.renderLocation;
                    data.length = tile.structure.volLength;
                    data.width = tile.structure.volWidth;
                    data.gasType = tile.structure.gasCoolantTank.getStack();
                    int glow = data.calculateGlowLight(light);
                    matrix.push();
                    matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
                    MekanismRenderer.renderObject(ModelRenderer.getModel(data, tile.prevCoolantScale), matrix, buffer, data.getColorARGB(tile.prevCoolantScale), glow);
                    matrix.pop();
                }
            }
            if (!tile.structure.heatedCoolantTank.isEmpty()) {
                GasRenderData data = new GasRenderData();
                data.height = tile.structure.volHeight - 2;
                if (data.height >= 1) {
                    data.location = tile.structure.renderLocation;
                    data.length = tile.structure.volLength;
                    data.width = tile.structure.volWidth;
                    data.gasType = tile.structure.heatedCoolantTank.getStack();
                    if (buffer == null) {
                        buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
                    }
                    matrix.push();
                    matrix.scale(0.998F, 0.998F, 0.998F);
                    matrix.translate(data.location.x - pos.getX() + 0.001, data.location.y - pos.getY() + 0.001, data.location.z - pos.getZ() + 0.001);
                    Model3D gasModel = ModelRenderer.getModel(data, 1);
                    MekanismRenderer.renderObject(gasModel, matrix, buffer, data.getColorARGB(tile.prevHeatedCoolantScale), data.calculateGlowLight(light));
                    matrix.pop();
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
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.renderLocation != null;
    }
}