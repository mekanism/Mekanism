package mekanism.generators.client.render;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelHeatGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHeatGenerator extends TileEntitySpecialRenderer<TileEntityHeatGenerator> {

    private ModelHeatGenerator model = new ModelHeatGenerator();

    @Override
    public void render(TileEntityHeatGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "HeatGenerator.png"));

        MekanismRenderer.rotate(tileEntity.getDirection(), 180, 0, 270, 90);

        GlStateManager.rotate(180, 0, 0, 1);
        model.render(0.0625F, tileEntity.getActive(), rendererDispatcher.renderEngine);
        GlStateManager.popMatrix();
    }
}