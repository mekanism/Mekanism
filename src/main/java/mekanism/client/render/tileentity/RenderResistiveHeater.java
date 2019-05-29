package mekanism.client.render.tileentity;

import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderResistiveHeater extends TileEntitySpecialRenderer<TileEntityResistiveHeater> {

    private ModelResistiveHeater model = new ModelResistiveHeater();

    @Override
    public void render(TileEntityResistiveHeater tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ResistiveHeater.png"));
        renderHelper.rotate(tileEntity.facing).rotateZ(180, 1);
        model.render(0.0625F, tileEntity.isActive, rendererDispatcher.renderEngine, true);
        renderHelper.cleanup();
    }
}