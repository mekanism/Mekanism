package mekanism.client.render.tileentity;

import mekanism.client.model.ModelChemicalCrystallizer;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderChemicalCrystallizer extends TileEntitySpecialRenderer<TileEntityChemicalCrystallizer> {

    private ModelChemicalCrystallizer model = new ModelChemicalCrystallizer();

    @Override
    public void render(TileEntityChemicalCrystallizer tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ChemicalCrystallizer.png"));

        MekanismRenderer.glRotateForFacing(tileEntity);

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        model.render(0.0625F);
        renderHelper.cleanup();

        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage, alpha);
    }
}