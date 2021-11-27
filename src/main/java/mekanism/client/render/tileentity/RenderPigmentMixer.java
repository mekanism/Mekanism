package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderPigmentMixer extends MekanismTileEntityRenderer<TileEntityPigmentMixer> {

    private static final float SHAFT_SPEED = 5F;

    public RenderPigmentMixer(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityPigmentMixer tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        //We only actually need to do rendering if the tile is active as that means we are using the active model
        // which has no shaft
        if (tile.getActive()) {
            matrix.pushPose();
            matrix.translate(6 / 16F, 0, 7 / 16F);
            float shift = 1 / 16F;
            matrix.translate(shift, 0, shift);
            matrix.mulPose(Vector3f.YN.rotationDegrees((tile.getLevel().getGameTime() + partialTick) * SHAFT_SPEED % 360));
            matrix.translate(-shift, 0, -shift);
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
}