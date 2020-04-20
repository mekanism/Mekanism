package mekanism.generators.client.render;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.block.attribute.Attribute;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderFissionFuelAssembly extends MekanismTileEntityRenderer<TileEntityFissionFuelAssembly> {

    private static Model3D glowModel;

    public RenderFissionFuelAssembly(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityFissionFuelAssembly tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (glowModel == null) {
            glowModel = new Model3D();
            glowModel.minX = 0.1; glowModel.minY = 0.01; glowModel.minZ = 0.1;
            glowModel.maxX = 0.9; glowModel.maxY = 0.99; glowModel.maxZ = 0.9;
            glowModel.setTexture(MekanismRenderer.whiteIcon);
        }

        if (Attribute.isActive(tile.getBlockState())) {
            matrix.push();
            IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
            int argb = MekanismRenderer.getColorARGB(0.466F, 0.882F, 0.929F, 0.8F);
            MekanismRenderer.renderObject(glowModel, matrix, buffer, argb, MekanismRenderer.FULL_LIGHT);
            matrix.pop();
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FISSION_FUEL_ASSEMBLY;
    }
}