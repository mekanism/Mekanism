package mekanism.client.render.tileentity;

import mekanism.client.model.ModelDigitalMiner;
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
        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));
        switch (tileEntity.facing) {
            case NORTH:
                GlStateManager.rotate(0, 0, 1, 0);
                GlStateManager.translate(0, 0, -1.0F);
                break;
            case SOUTH:
                GlStateManager.rotate(180, 0, 1, 0);
                GlStateManager.translate(0, 0, -1.0F);
                break;
            case WEST:
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.translate(0, 0, -1.0F);
                break;
            case EAST:
                GlStateManager.rotate(270, 0, 1, 0);
                GlStateManager.translate(0, 0, -1.0F);
                break;
        }

        GlStateManager.rotate(180, 0, 0, 1);
        model.render(0.0625F, tileEntity.isActive, rendererDispatcher.renderEngine, true);
        GlStateManager.popMatrix();

        if (tileEntity.clientRendering) {
            MinerVisualRenderer.render(tileEntity);
        }
    }
}