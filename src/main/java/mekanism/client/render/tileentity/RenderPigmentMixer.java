package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Outlines;
import mekanism.client.render.lib.Outlines.Line;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderPigmentMixer extends MekanismTileEntityRenderer<TileEntityPigmentMixer> implements IWireFrameRenderer {

    private static final float SHAFT_SPEED = 5F;
    @Nullable
    private static List<Line> lines;

    public static void resetCached() {
        lines = null;
    }

    public RenderPigmentMixer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityPigmentMixer tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        setupRenderer(tile, partialTick, matrix);
        Pose entry = matrix.last();
        VertexConsumer buffer = renderer.getBuffer(Sheets.solidBlockSheet());
        for (BakedQuad quad : MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.getQuads(tile.getLevel().random)) {
            buffer.putBulkData(entry, quad, 1, 1, 1, 1, light, overlayLight);
        }
        matrix.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PIGMENT_MIXER;
    }

    @Override
    public boolean shouldRender(TileEntityPigmentMixer tile, Vec3 camera) {
        //We only actually need to do rendering if the tile is active as that means we are using the active model which has no shaft
        return tile.getActive() && super.shouldRender(tile, camera);
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
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer) {
        if (tile instanceof TileEntityPigmentMixer mixer) {
            if (lines == null) {
                lines = Outlines.extract(MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.getBakedModel(), null, tile.getLevel().random, ModelData.EMPTY, null);
            }
            setupRenderer(mixer, partialTick, matrix);
            Pose pose = matrix.last();
            RenderTickHandler.renderVertexWireFrame(lines, buffer, pose.pose(), pose.normal());
            matrix.popPose();
        }
    }

    private void setupRenderer(TileEntityPigmentMixer tile, float partialTick, PoseStack matrix) {
        matrix.pushPose();
        switch (tile.getDirection()) {
            case NORTH -> matrix.translate(7 / 16F, 0, 6 / 16F);
            case SOUTH -> matrix.translate(7 / 16F, 0, 0.5F);
            case WEST -> matrix.translate(6 / 16F, 0, 7 / 16F);
            case EAST -> matrix.translate(0.5F, 0, 7 / 16F);
        }
        float shift = 1 / 16F;
        matrix.translate(shift, 0, shift);
        matrix.mulPose(Axis.YN.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * SHAFT_SPEED % 360));
        matrix.translate(-shift, 0, -shift);
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityPigmentMixer tile) {
        //We only care about the position that is above because we only use the BER to render the shaft which is in the upper block
        return new AABB(tile.getBlockPos().above());
    }
}