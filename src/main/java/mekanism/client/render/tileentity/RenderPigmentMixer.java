package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
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
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderPigmentMixer extends MekanismTileEntityRenderer<TileEntityPigmentMixer> implements IWireFrameRenderer {

    private static final List<Vertex[]> vertices = new ArrayList<>();
    private static final float SHAFT_SPEED = 5F;

    public static void resetCached() {
        vertices.clear();
    }

    public RenderPigmentMixer(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityPigmentMixer tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        //We only actually need to do rendering if the tile is active as that means we are using the active model
        // which has no shaft
        if (tile.getActive()) {
            performTranslations(tile, partialTick, matrix);
            Entry entry = matrix.last();
            IVertexBuilder buffer = renderer.getBuffer(Atlases.solidBlockSheet());
            for (BakedQuad quad : MekanismModelCache.INSTANCE.PIGMENT_MIXER_SHAFT.getBakedModel().getQuads(null, null, tile.getLevel().random)) {
                buffer.addVertexData(entry, quad, 1F, 1F, 1F, 1F, light, overlayLight);
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
    public void renderWireFrame(TileEntity tile, float partialTick, MatrixStack matrix, IVertexBuilder buffer, float red, float green, float blue, float alpha) {
        if (tile instanceof TileEntityPigmentMixer) {
            performTranslations((TileEntityPigmentMixer) tile, partialTick, matrix);
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
     * Make sure to call {@link MatrixStack#popPose()} afterwards
     */
    private void performTranslations(TileEntityPigmentMixer tile, float partialTick, MatrixStack matrix) {
        matrix.pushPose();
        switch (tile.getDirection()) {
            case NORTH:
                matrix.translate(7 / 16F, 0, 6 / 16F);
                break;
            case SOUTH:
                matrix.translate(7 / 16F, 0, 0.5F);
                break;
            case WEST:
                matrix.translate(6 / 16F, 0, 7 / 16F);
                break;
            case EAST:
                matrix.translate(0.5F, 0, 7 / 16F);
                break;
        }
        float shift = 1 / 16F;
        matrix.translate(shift, 0, shift);
        matrix.mulPose(Vector3f.YN.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * SHAFT_SPEED % 360));
        matrix.translate(-shift, 0, -shift);
    }
}