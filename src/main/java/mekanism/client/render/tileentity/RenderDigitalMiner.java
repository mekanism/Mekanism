package mekanism.client.render.tileentity;

import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MinerVisualRenderer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDigitalMiner extends TileEntitySpecialRenderer<TileEntityDigitalMiner> {

    private ModelDigitalMiner model = new ModelDigitalMiner();

    @Override
    public void render(TileEntityDigitalMiner tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);

        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));

        switch (tileEntity.facing.ordinal()) {
            case 2:
                renderHelper.rotateY(0, 1).translateZ(-1.0F);
                break;
            case 3:
                renderHelper.rotateY(180, 1).translateZ(-1.0F);
                break;
            case 4:
                renderHelper.rotateY(90, 1).translateZ(-1.0F);
                break;
            case 5:
                renderHelper.rotateY(270, 1).translateZ(-1.0F);
                break;
        }

        renderHelper.rotateZ(180, 1);
        model.render(0.0625F, tileEntity.isActive, rendererDispatcher.renderEngine, true);
        renderHelper.cleanup();

        if (tileEntity.clientRendering) {
            MinerVisualRenderer.render(tileEntity);
        }
    }
}