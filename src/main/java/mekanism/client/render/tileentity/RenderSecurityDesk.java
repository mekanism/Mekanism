package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderSecurityDesk extends MekanismTileEntityRenderer<TileEntitySecurityDesk> {

    private ModelSecurityDesk model = new ModelSecurityDesk();

    @Override
    public void func_225616_a_(@Nonnull TileEntitySecurityDesk tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "security_desk.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        GlStateManager.rotatef(180, 0, 0, 1);
        setLightmapDisabled(true);
        model.render(0.0625F, rendererDispatcher.textureManager);
        setLightmapDisabled(false);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySecurityDesk tile) {
        return true;
    }
}