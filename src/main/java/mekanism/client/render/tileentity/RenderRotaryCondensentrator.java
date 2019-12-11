package mekanism.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.model.ModelRotaryCondensentrator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

//TODO: Remove unused?
public class RenderRotaryCondensentrator extends TileEntityRenderer<TileEntityRotaryCondensentrator> {

    private ModelRotaryCondensentrator model = new ModelRotaryCondensentrator();

    @Override
    public void render(TileEntityRotaryCondensentrator tile, double x, double y, double z, float partialTick, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "rotary_condensentrator.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        GlStateManager.rotatef(180, 0, 0, 1);
        model.render(0.0625F);
        GlStateManager.popMatrix();
    }
}