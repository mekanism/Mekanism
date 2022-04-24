package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderIndustrialAlarm extends MekanismTileEntityRenderer<TileEntityIndustrialAlarm> {

    private static final float ROTATE_SPEED = 10F;
    private final ModelIndustrialAlarm model;

    public RenderIndustrialAlarm(BlockEntityRendererProvider.Context context) {
        super(context);
        model = new ModelIndustrialAlarm(context.getModelSet());
    }

    @Override
    protected void render(TileEntityIndustrialAlarm tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        performTranslations(tile, matrix);
        float rotation = (tile.getLevel().getGameTime() + partialTick) * ROTATE_SPEED % 360;
        model.render(matrix, renderer, light, overlayLight, Attribute.isActive(tile.getBlockState()), rotation, false, false);
        matrix.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.INDUSTRIAL_ALARM;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityIndustrialAlarm tile) {
        return true;
    }

    /**
     * Make sure to call {@link PoseStack#popPose()} afterwards
     */
    private void performTranslations(TileEntityIndustrialAlarm tile, PoseStack matrix) {
        matrix.pushPose();
        matrix.translate(0.5, 0, 0.5);
        switch (tile.getDirection()) {
            case DOWN -> {
                matrix.translate(0, 1, 0);
                matrix.mulPose(Vector3f.XP.rotationDegrees(180));
            }
            case NORTH -> {
                matrix.translate(0, 0.5, 0.5);
                matrix.mulPose(Vector3f.XN.rotationDegrees(90));
            }
            case SOUTH -> {
                matrix.translate(0, 0.5, -0.5);
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
            }
            case EAST -> {
                matrix.translate(-0.5, 0.5, 0);
                matrix.mulPose(Vector3f.ZN.rotationDegrees(90));
            }
            case WEST -> {
                matrix.translate(0.5, 0.5, 0);
                matrix.mulPose(Vector3f.ZP.rotationDegrees(90));
            }
        }
    }
}
