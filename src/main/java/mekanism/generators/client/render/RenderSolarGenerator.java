package mekanism.generators.client.render;

import mekanism.client.render.MekanismRenderHelper;
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
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);

        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarGenerator.png"));

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        model.render(0.0625F);
        renderHelper.cleanup();
    }
}