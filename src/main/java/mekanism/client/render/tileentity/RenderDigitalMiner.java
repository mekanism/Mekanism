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
        GLSMHelper.INSTANCE.translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));
        switch (tileEntity.facing.ordinal()) {
            case 2:
                GLSMHelper.INSTANCE.rotateY(0, 1).translateZ(-1.0F);
                break;
            case 3:
                GLSMHelper.INSTANCE.rotateY(180, 1).translateZ(-1.0F);
                break;
            case 4:
                GLSMHelper.INSTANCE.rotateY(90, 1).translateZ(-1.0F);
                break;
            case 5:
                GLSMHelper.INSTANCE.rotateY(270, 1).translateZ(-1.0F);
                break;
        }

        GLSMHelper.INSTANCE.rotateZ(180, 1);
        model.render(0.0625F, tileEntity.isActive, rendererDispatcher.renderEngine, true);
        GlStateManager.popMatrix();

        if (tileEntity.clientRendering) {
            MinerVisualRenderer.render(tileEntity);
        }
    }
}