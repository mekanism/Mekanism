package mekanism.generators.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelHeatGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class RenderHeatGenerator extends TileEntityRenderer<TileEntityHeatGenerator> {

    private ModelHeatGenerator model = new ModelHeatGenerator();

    @Override
    public void render(TileEntityHeatGenerator tile, double x, double y, double z, float partialTick, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "heat_generator.png"));

        MekanismRenderer.rotate(tile.getDirection(), 180, 0, 270, 90);

        GlStateManager.rotatef(180, 0, 0, 1);
        model.render(0.0625F, tile.getActive(), rendererDispatcher.textureManager);
        GlStateManager.popMatrix();
    }
}