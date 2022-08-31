package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;

@NothingNullByDefault
public class RenderSolarNeutronActivator extends ModelTileEntityRenderer<TileEntitySolarNeutronActivator, ModelSolarNeutronActivator> implements IWireFrameRenderer {

    public RenderSolarNeutronActivator(BlockEntityRendererProvider.Context context) {
        super(context, ModelSolarNeutronActivator::new);
    }

    @Override
    protected void render(TileEntitySolarNeutronActivator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        renderTranslated(tile, matrix, poseStack -> model.render(poseStack, renderer, light, overlayLight, false));
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SOLAR_NEUTRON_ACTIVATOR;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntitySolarNeutronActivator tile) {
        return true;
    }

    @Override
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, int red, int green, int blue, int alpha) {
        if (tile instanceof TileEntitySolarNeutronActivator sna) {
            renderTranslated(sna, matrix, poseStack -> model.renderWireFrame(poseStack, buffer, red, green, blue, alpha));
        }
    }

    private void renderTranslated(TileEntitySolarNeutronActivator tile, PoseStack matrix, Consumer<PoseStack> renderer) {
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        renderer.accept(matrix);
        matrix.popPose();
    }
}