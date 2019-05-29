package mekanism.client.render.tileentity;

import mekanism.client.model.ModelChemicalDissolutionChamber;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChemicalDissolutionChamber extends TileEntitySpecialRenderer<TileEntityChemicalDissolutionChamber> {

    private ModelChemicalDissolutionChamber model = new ModelChemicalDissolutionChamber();

    @Override
    public void render(TileEntityChemicalDissolutionChamber tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalDissolutionChamber.png"));
        renderHelper.rotate(tileEntity.facing).rotateZ(180, 1);
        model.render(0.0625F);
        renderHelper.cleanup();
    }
}