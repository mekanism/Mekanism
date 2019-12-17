package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderResistiveHeater extends MekanismTileEntityRenderer<TileEntityResistiveHeater> {

    private ModelResistiveHeater model = new ModelResistiveHeater();

    @Override
    public void func_225616_a_(@Nonnull TileEntityResistiveHeater tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "resistive_heater.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        GlStateManager.rotatef(180, 0, 0, 1);
        model.render(0.0625F, tile.getActive(), rendererDispatcher.textureManager, true);
        GlStateManager.popMatrix();
    }
}