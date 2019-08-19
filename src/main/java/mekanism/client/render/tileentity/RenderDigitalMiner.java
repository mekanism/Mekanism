package mekanism.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MinerVisualRenderer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderDigitalMiner extends TileEntityRenderer<TileEntityDigitalMiner> {

    private ModelDigitalMiner model = new ModelDigitalMiner();

    @Override
    public void render(TileEntityDigitalMiner tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "digital_miner.png"));

        MekanismRenderer.rotate(tileEntity.getDirection(), 0, 180, 90, 270);
        GlStateManager.translatef(0, 0, -1.0F);

        GlStateManager.rotatef(180, 0, 0, 1);
        model.render(0.0625F, tileEntity.getActive(), rendererDispatcher.textureManager, true);
        GlStateManager.popMatrix();

        if (tileEntity.clientRendering) {
            MinerVisualRenderer.render(tileEntity);
        }
    }
}