package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntitySecurityDesk;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderSecurityDesk extends MekanismTileEntityRenderer<TileEntitySecurityDesk> {

    private ModelSecurityDesk model = new ModelSecurityDesk();

    public RenderSecurityDesk(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntitySecurityDesk tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        model.render(matrix, renderer, light, overlayLight);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SECURITY_DESK;
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySecurityDesk tile) {
        return true;
    }
}