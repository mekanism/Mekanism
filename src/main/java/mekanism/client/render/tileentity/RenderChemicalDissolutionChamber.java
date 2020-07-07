package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelChemicalDissolutionChamber;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderChemicalDissolutionChamber extends MekanismTileEntityRenderer<TileEntityChemicalDissolutionChamber> {

    private final ModelChemicalDissolutionChamber model = new ModelChemicalDissolutionChamber();

    public RenderChemicalDissolutionChamber(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityChemicalDissolutionChamber tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        model.render(matrix, renderer, light, overlayLight, false);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.CHEMICAL_DISSOLUTION_CHAMBER;
    }
}