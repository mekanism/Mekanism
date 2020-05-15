package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public final class MinerVisualRenderer {

    private static final float SCALE_FIX = 0.9999F;
    private static final float OFFSET_FIX = 0.00005F;
    private static Model3D model;

    public static void resetCachedVisuals() {
        model = null;
    }

    public static void render(@Nonnull TileEntityDigitalMiner miner, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer) {
        if (miner.getRadius() <= 64) {
            if (model == null) {
                model = new Model3D();
                model.setTexture(MekanismRenderer.whiteIcon);
                model.minX = 0;
                model.minY = 0;
                model.minZ = 0;
                model.maxX = 1;
                model.maxY = 1;
                model.maxZ = 1;
            }
            //TODO: Eventually we may want to make it so that the model can support each face being a different
            // color to make it easier to see the "depth"
            matrix.push();
            matrix.translate(-miner.getRadius(), miner.getMinY() - miner.getPos().getY(), -miner.getRadius());
            matrix.scale(miner.getDiameter(), miner.getMaxY() - miner.getMinY(), miner.getDiameter());
            //Adjust it slightly so that it does not clip into the blocks that are just on the outside of the radius
            matrix.scale(SCALE_FIX, SCALE_FIX, SCALE_FIX);
            matrix.translate(OFFSET_FIX, OFFSET_FIX, OFFSET_FIX);
            MekanismRenderer.renderObject(model, matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()),
                  MekanismRenderer.getColorARGB(255, 255, 255, 0.8F), MekanismRenderer.FULL_LIGHT);
            matrix.pop();
        }
    }
}