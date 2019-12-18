package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.common.tile.TileEntitySecurityDesk;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderSecurityDesk extends MekanismTileEntityRenderer<TileEntitySecurityDesk> {

    private ModelSecurityDesk model = new ModelSecurityDesk();

    @Override
    public void func_225616_a_(@Nonnull TileEntitySecurityDesk tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        field_228858_b_.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "security_desk.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        RenderSystem.rotatef(180, 0, 0, 1);
        setLightmapDisabled(true);
        model.render(0.0625F, field_228858_b_.textureManager);
        setLightmapDisabled(false);
        RenderSystem.popMatrix();*/
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySecurityDesk tile) {
        return true;
    }
}