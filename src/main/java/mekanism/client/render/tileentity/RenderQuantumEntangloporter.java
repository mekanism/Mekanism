package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderQuantumEntangloporter extends MekanismTileEntityRenderer<TileEntityQuantumEntangloporter> {

    private final ModelQuantumEntangloporter model;

    public RenderQuantumEntangloporter(BlockEntityRendererProvider.Context context) {
        super(context);
        model = new ModelQuantumEntangloporter(context.getModelSet());
    }

    @Override
    protected void render(TileEntityQuantumEntangloporter tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        model.render(matrix, renderer, light, overlayLight, false, false);
        matrix.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.QUANTUM_ENTANGLOPORTER;
    }
}