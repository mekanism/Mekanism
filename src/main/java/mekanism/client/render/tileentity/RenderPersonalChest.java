package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderPersonalChest extends MekanismTileEntityRenderer<TileEntityPersonalChest> {

    //TODO: 1.15
    //private ChestModel model = new ChestModel();

    @Override
    public void func_225616_a_(@Nonnull TileEntityPersonalChest tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x, (float) y + 1F, (float) z);
        RenderSystem.rotatef(90, 0, 1, 0);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "personal_chest.png"));

        MekanismRenderer.rotate(tile.getDirection(), 270, 90, 0, 180);
        switch (tile.getDirection()) {
            case NORTH:
                RenderSystem.translatef(1.0F, 0, 0);
                break;
            case SOUTH:
                RenderSystem.translatef(0, 0, -1.0F);
                break;
            case EAST:
                RenderSystem.translatef(1.0F, 0, -1.0F);
                break;
        }

        float lidangle = tile.prevLidAngle + (tile.lidAngle - tile.prevLidAngle) * partialTick;
        lidangle = 1.0F - lidangle;
        lidangle = 1.0F - lidangle * lidangle * lidangle;
        model.getLid().rotateAngleX = -((lidangle * 3.141593F) / 2.0F);
        RenderSystem.rotatef(180, 0, 0, 1);
        model.renderAll();
        RenderSystem.popMatrix();
    }
}