package mekanism.client.render.tileentity;

import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.MekanismRenderHelper;
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
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);

        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));

        switch (tileEntity.facing.ordinal()) {
            case 2:
                GlStateManager.rotate(0, 0.0F, 1.0F, 0.0F);
                renderHelper.translateZ(-1.0F);
                break;
            case 3:
                GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
                renderHelper.translateZ(-1.0F);
                break;
            case 4:
                GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
                renderHelper.translateZ(-1.0F);
                break;
            case 5:
                GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F);
                renderHelper.translateZ(-1.0F);
                break;
        }

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        model.render(0.0625F, tileEntity.isActive, rendererDispatcher.renderEngine, true);
        renderHelper.cleanup();

        if (tileEntity.clientRendering) {
            MinerVisualRenderer.render(tileEntity);
        }
    }
}