package mekanism.generators.client.render;

import mekanism.client.render.GLSMHelper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAdvancedSolarGenerator extends TileEntitySpecialRenderer<TileEntityAdvancedSolarGenerator> {

    private ModelAdvancedSolarGenerator model = new ModelAdvancedSolarGenerator();

    @Override
    public void render(TileEntityAdvancedSolarGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AdvancedSolarGenerator.png"));
        GLSMHelper.rotate(tileEntity.facing);
        GlStateManager.rotate(180, 0, 0, 1);
        model.render(0.0625F);
        GlStateManager.popMatrix();
    }
}