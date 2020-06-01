package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderResistiveHeater extends MekanismTileEntityRenderer<TileEntityResistiveHeater> {

    private final ModelResistiveHeater model = new ModelResistiveHeater();

    public RenderResistiveHeater(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityResistiveHeater tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        model.render(matrix, renderer, light, overlayLight, tile.getActive(), false);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.RESISTIVE_HEATER;
    }
}