package mekanism.client.render.tileentity;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelChest;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderPersonalChest extends TileEntityRenderer<TileEntityPersonalChest> {

    private ModelChest model = new ModelChest();

    @Override
    public void render(TileEntityPersonalChest tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x, (float) y + 1F, (float) z);
        GlStateManager.rotatef(90, 0, 1, 0);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PersonalChest.png"));

        MekanismRenderer.rotate(tileEntity.getDirection(), 270, 90, 0, 180);
        switch (tileEntity.getDirection()) {
            case NORTH:
                GlStateManager.translatef(1.0F, 0, 0);
                break;
            case SOUTH:
                GlStateManager.translatef(0, 0, -1.0F);
                break;
            case EAST:
                GlStateManager.translatef(1.0F, 0, -1.0F);
                break;
        }

        float lidangle = tileEntity.prevLidAngle + (tileEntity.lidAngle - tileEntity.prevLidAngle) * partialTick;
        lidangle = 1.0F - lidangle;
        lidangle = 1.0F - lidangle * lidangle * lidangle;
        model.chestLid.rotateAngleX = -((lidangle * 3.141593F) / 2.0F);
        GlStateManager.rotatef(180, 0, 0, 1);
        model.renderAll();
        GlStateManager.popMatrix();
    }
}