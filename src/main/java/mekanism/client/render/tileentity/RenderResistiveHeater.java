package mekanism.client.render.tileentity;

import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderResistiveHeater extends TileEntitySpecialRenderer<TileEntityResistiveHeater> {

    private ModelResistiveHeater model = new ModelResistiveHeater();

    @Override
    public void render(TileEntityResistiveHeater tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ResistiveHeater.png"));
        MekanismRenderer.rotate(tileEntity.getDirection(), 0, 180, 90, 270);
        GlStateManager.rotate(180, 0, 0, 1);
        model.render(0.0625F, tileEntity.getActive(), rendererDispatcher.renderEngine, true);
        GlStateManager.popMatrix();
    }
}