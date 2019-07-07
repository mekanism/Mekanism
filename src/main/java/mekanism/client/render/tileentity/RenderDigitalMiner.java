package mekanism.client.render.tileentity;

import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MinerVisualRenderer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDigitalMiner extends TileEntitySpecialRenderer<TileEntityDigitalMiner> {

    private ModelDigitalMiner model = new ModelDigitalMiner();

    @Override
    public void render(TileEntityDigitalMiner tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));

        GLSMHelper.rotate(tileEntity.facing, 0, 180, 90, 270);
        GlStateManager.translate(0, 0, -1.0F);

        GlStateManager.rotate(180, 0, 0, 1);
        model.render(0.0625F, tileEntity.isActive, rendererDispatcher.renderEngine, true);
        GlStateManager.popMatrix();

        if (tileEntity.clientRendering) {
            MinerVisualRenderer.render(tileEntity);
        }
    }
}