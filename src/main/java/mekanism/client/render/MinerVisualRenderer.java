package mekanism.client.render;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public final class MinerVisualRenderer {

    private static Model3D model;

    public static void render(@Nonnull TileEntityDigitalMiner miner, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer) {
        if (miner.getRadius() <= 64) {
            //TODO: Eventually we may want to make it so that the model can support each face being a different
            // color to make it easier to see the "depth"
            if (model == null) {
                model = new Model3D();
                model.setTexture(MekanismRenderer.whiteIcon);
                model.minX = 0.01;
                model.minY = 0.01;
                model.minZ = 0.01;
                model.maxX = 0.99;
                model.maxY = 0.99;
                model.maxZ = 0.99;
            }
            matrix.push();
            matrix.translate(-miner.getRadius(), miner.getMinY() - miner.getPos().getY(), -miner.getRadius());
            matrix.scale(1 + miner.getRadius() * 2, miner.getMaxY() - miner.getMinY() + 1, 1 + miner.getRadius() * 2);
            MekanismRenderer.renderObject(model, matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()),
                  MekanismRenderer.getColorARGB(255, 255, 255, 0.8F), MekanismRenderer.FULL_LIGHT);
            matrix.pop();
        }
    }
}