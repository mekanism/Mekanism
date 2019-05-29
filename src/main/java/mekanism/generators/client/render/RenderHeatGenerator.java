package mekanism.generators.client.render;

import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelHeatGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHeatGenerator extends TileEntitySpecialRenderer<TileEntityHeatGenerator> {

    private ModelHeatGenerator model = new ModelHeatGenerator();

    @Override
    public void render(TileEntityHeatGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "HeatGenerator.png"));

        switch (tileEntity.facing.ordinal()) {
            case 2:
                renderHelper.rotateY(180, 1);
                break;
            case 3:
                renderHelper.rotateY(0, 1);
                break;
            case 4:
                renderHelper.rotateY(270, 1);
                break;
            case 5:
                renderHelper.rotateY(90,  1);
                break;
        }

        renderHelper.rotateZ(180, 1);
        model.render(0.0625F, tileEntity.getActive(), rendererDispatcher.renderEngine);
        renderHelper.cleanup();
    }
}