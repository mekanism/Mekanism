package mekanism.generators.client.render;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelSolarGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RenderSolarGenerator extends TileEntitySpecialRenderer<TileEntitySolarGenerator> {

    private ModelSolarGenerator model = new ModelSolarGenerator();

    @Override
    public void render(TileEntitySolarGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarGenerator.png"));
        GlStateManager.rotate(180, 0, 0, 1);
        model.render(0.0625F);
        GlStateManager.popMatrix();
    }
}