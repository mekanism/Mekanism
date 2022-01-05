package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;

@ParametersAreNonnullByDefault
public class RenderSolarNeutronActivator extends MekanismTileEntityRenderer<TileEntitySolarNeutronActivator> implements IWireFrameRenderer {

    private final ModelSolarNeutronActivator model;

    public RenderSolarNeutronActivator(BlockEntityRendererProvider.Context context) {
        super(context);
        model = new ModelSolarNeutronActivator(context.getModelSet());
    }

    @Override
    protected void render(TileEntitySolarNeutronActivator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        performTranslations(tile, matrix);
        model.render(matrix, renderer, light, overlayLight, false);
        matrix.popPose();
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
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, float red, float green, float blue, float alpha) {
        if (tile instanceof TileEntitySolarNeutronActivator sna) {
            performTranslations(sna, matrix);
            model.renderWireFrame(matrix, buffer, red, green, blue, alpha);
            matrix.popPose();
        }
    }

    /**
     * Make sure to call {@link PoseStack#popPose()} afterwards
     */
    private void performTranslations(TileEntitySolarNeutronActivator tile, PoseStack matrix) {
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
    }
}