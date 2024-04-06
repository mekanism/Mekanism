package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Vertex;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

@NothingNullByDefault
public class RenderSeismicVibrator extends MekanismTileEntityRenderer<TileEntitySeismicVibrator> implements IWireFrameRenderer {

    private static final List<Vertex[]> vertices = new ArrayList<>();

    public static void resetCached() {
        vertices.clear();
    }

    public RenderSeismicVibrator(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntitySeismicVibrator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        setupRenderer(tile, partialTick, matrix);
        Pose entry = matrix.last();
        VertexConsumer buffer = renderer.getBuffer(Sheets.solidBlockSheet());
        for (BakedQuad quad : MekanismModelCache.INSTANCE.VIBRATOR_SHAFT.getQuads(tile.getLevel().random)) {
            buffer.putBulkData(entry, quad, 1, 1, 1, light, overlayLight);
        }
        matrix.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SEISMIC_VIBRATOR;
    }

    @Override
    public boolean isCombined() {
        return true;
    }

    @Override
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, int red, int green, int blue, int alpha) {
        if (tile instanceof TileEntitySeismicVibrator vibrator) {
            if (vertices.isEmpty()) {
                MekanismModelCache.INSTANCE.VIBRATOR_SHAFT.collectQuadVertices(vertices, tile.getLevel().random);
            }
            setupRenderer(vibrator, partialTick, matrix);
            RenderTickHandler.renderVertexWireFrame(vertices, buffer, matrix.last().pose(), red, green, blue, alpha);
            matrix.popPose();
        }
    }

    private void setupRenderer(TileEntitySeismicVibrator tile, float partialTick, PoseStack matrix) {
        matrix.pushPose();
        float piston = Math.max(0, (float) Math.sin((tile.clientPiston + (tile.getActive() ? partialTick : 0)) / 5F));
        matrix.translate(0, piston * 0.625, 0);
    }

    @Override
    public AABB getRenderBoundingBox(TileEntitySeismicVibrator tile) {
        BlockPos pos = tile.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos, pos.above());
    }
}