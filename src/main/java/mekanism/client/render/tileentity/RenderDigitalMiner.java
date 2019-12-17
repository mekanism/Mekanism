package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MinerVisualRenderer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderDigitalMiner extends MekanismTileEntityRenderer<TileEntityDigitalMiner> {

    private ModelDigitalMiner model = new ModelDigitalMiner();

    @Override
    public void func_225616_a_(@Nonnull TileEntityDigitalMiner tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        setLightmapDisabled(true);
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "digital_miner.png"));

        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        GlStateManager.translatef(0, 0, -1.0F);

        GlStateManager.rotatef(180, 0, 0, 1);
        model.render(0.0625F, tile.getActive(), rendererDispatcher.textureManager, true);
        GlStateManager.popMatrix();

        if (tile.clientRendering) {
            MinerVisualRenderer.render(tile);
        }
        setLightmapDisabled(false);
    }

    @Override
    public boolean isGlobalRenderer(TileEntityDigitalMiner tile) {
        return true;
    }
}