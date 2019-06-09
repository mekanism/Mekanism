package mekanism.client.render.tileentity;

import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.render.GLSMHelper;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSecurityDesk extends TileEntitySpecialRenderer<TileEntitySecurityDesk> {

    private ModelSecurityDesk model = new ModelSecurityDesk();

    @Override
    public void render(TileEntitySecurityDesk tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GLSMHelper.INSTANCE.translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SecurityDesk.png"));
        GLSMHelper.INSTANCE.rotate(tileEntity.facing).rotateZ(180, 1);
        model.render(0.0625F, rendererDispatcher.renderEngine);
        GlStateManager.popMatrix();
    }
}