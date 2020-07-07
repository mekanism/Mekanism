package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderSolarNeutronActivator extends MekanismTileEntityRenderer<TileEntitySolarNeutronActivator> implements IWireFrameRenderer {

    private final ModelSolarNeutronActivator model = new ModelSolarNeutronActivator();

    public RenderSolarNeutronActivator(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntitySolarNeutronActivator tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        performTranslations(tile, matrix);
        model.render(matrix, renderer, light, overlayLight, false);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SOLAR_NEUTRON_ACTIVATOR;
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySolarNeutronActivator tile) {
        return true;
    }

    @Override
    public void renderWireFrame(TileEntity tile, float partialTick, MatrixStack matrix, IVertexBuilder buffer, float red, float green, float blue, float alpha) {
        if (tile instanceof TileEntitySolarNeutronActivator) {
            performTranslations((TileEntitySolarNeutronActivator) tile, matrix);
            model.renderWireFrame(matrix, buffer, red, green, blue, alpha);
            matrix.pop();
        }
    }

    /**
     * Make sure to call matrix.pop afterwards
     */
    private void performTranslations(TileEntitySolarNeutronActivator tile, MatrixStack matrix) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
    }
}