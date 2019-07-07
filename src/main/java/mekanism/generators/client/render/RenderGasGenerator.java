package mekanism.generators.client.render;

import mekanism.client.render.GLSMHelper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelGasGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGasGenerator extends TileEntitySpecialRenderer<TileEntityGasGenerator> {

    private ModelGasGenerator model = new ModelGasGenerator();

    @Override
    public void render(TileEntityGasGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasGenerator.png"));

        GLSMHelper.rotate(tileEntity.facing, 90, 270, 180, 0);

        GlStateManager.rotate(180, 0, 1, 1);
        GlStateManager.rotate(90, -1, 0, 0);
        GlStateManager.rotate(90, 0, 1, 0);
        model.render(0.0625F);
        GlStateManager.popMatrix();
    }
}