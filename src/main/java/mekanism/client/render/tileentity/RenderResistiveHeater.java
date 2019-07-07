package mekanism.client.render.tileentity;

import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.render.GLSMHelper;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderResistiveHeater extends TileEntitySpecialRenderer<TileEntityResistiveHeater> {

    private ModelResistiveHeater model = new ModelResistiveHeater();

    @Override
    public void render(TileEntityResistiveHeater tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ResistiveHeater.png"));
        GLSMHelper.rotate(tileEntity.facing);
        GlStateManager.rotate(180, 0, 0, 1);
        model.render(0.0625F, tileEntity.isActive, rendererDispatcher.renderEngine, true);
        GlStateManager.popMatrix();
    }
}