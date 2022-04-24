package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.Vertex;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@ParametersAreNonnullByDefault
public class RenderPigmentMixer extends MekanismTileEntityRenderer<TileEntityPigmentMixer> implements IWireFrameRenderer {

    private static final List<Vertex[]> vertices = new ArrayList<>();
    private static final float SHAFT_SPEED = 5F;

    public static void resetCached() {
        vertices.clear();
    }

    public RenderPigmentMixer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityPigmentMixer tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        //We only actually need to do rendering if the tile is active as that means we are using the active model
        // which has no shaft
        if (tile.getActive()) {
            performTranslations(tile, partialTick, matrix);
            Pose entry = matrix.last();
            VertexConsumer buffer = renderer.getBuffer(Sheets.solidBlockSheet());
            for (BakedQuad quad : MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.getBakedModel().getQuads(null, null, tile.getLevel().random)) {
                buffer.putBulkData(entry, quad, 1F, 1F, 1F, 1F, light, overlayLight);
            }
            matrix.popPose();
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PIGMENT_MIXER;
    }

    @Override
    public boolean hasSelectionBox(BlockState state) {
        return Attribute.isActive(state);
    }

    @Override
    public boolean isCombined() {
        return true;
    }

    @Override
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, float red, float green, float blue, float alpha) {
        if (tile instanceof TileEntityPigmentMixer mixer) {
            performTranslations(mixer, partialTick, matrix);
            if (vertices.isEmpty()) {
                for (Quad quad : QuadUtils.unpack(MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.getBakedModel().getQuads(null, null, tile.getLevel().random))) {
                    vertices.add(quad.getVertices());
                }
            }
            RenderTickHandler.renderVertexWireFrame(vertices, buffer, matrix.last().pose(), red, green, blue, alpha);
            matrix.popPose();
        }
    }

    /**
     * Make sure to call {@link PoseStack#popPose()} afterwards
     */
    private void performTranslations(TileEntityPigmentMixer tile, float partialTick, PoseStack matrix) {
        matrix.pushPose();
        switch (tile.getDirection()) {
            case NORTH -> matrix.translate(7 / 16F, 0, 6 / 16F);
            case SOUTH -> matrix.translate(7 / 16F, 0, 0.5F);
            case WEST -> matrix.translate(6 / 16F, 0, 7 / 16F);
            case EAST -> matrix.translate(0.5F, 0, 7 / 16F);
        }
        float shift = 1 / 16F;
        matrix.translate(shift, 0, shift);
        matrix.mulPose(Vector3f.YN.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * SHAFT_SPEED % 360));
        matrix.translate(-shift, 0, -shift);
    }
}