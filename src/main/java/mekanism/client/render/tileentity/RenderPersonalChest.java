package mekanism.client.render.tileentity;

import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPersonalChest extends TileEntitySpecialRenderer<TileEntityPersonalChest> {

    private ModelChest model = new ModelChest();

    @Override
    public void render(TileEntityPersonalChest tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x, y + 1, z).rotateY(90, 1);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PersonalChest.png"));

        switch (tileEntity.facing.ordinal()) {
            case 2:
                renderHelper.rotateY(270, 1).translateX(1.0F);
                break;
            case 3:
                renderHelper.rotateY(90, 1).translateZ(-1.0F);
                break;
            case 4:
                renderHelper.rotateY(0, 1);
                break;
            case 5:
                renderHelper.rotateY(180, 1).translateXZ(1.0F, -1.0F);
                break;
        }

        float lidangle = tileEntity.prevLidAngle + (tileEntity.lidAngle - tileEntity.prevLidAngle) * partialTick;
        lidangle = 1.0F - lidangle;
        lidangle = 1.0F - lidangle * lidangle * lidangle;
        model.chestLid.rotateAngleX = -((lidangle * 3.141593F) / 2.0F);
        renderHelper.rotateZ(180, 1);
        model.renderAll();
        renderHelper.cleanup();
    }
}