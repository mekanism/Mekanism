package mekanism.generators.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderAdvancedSolarGenerator extends TileEntityRenderer<TileEntityAdvancedSolarGenerator> {

    private ModelAdvancedSolarGenerator model = new ModelAdvancedSolarGenerator();

    @Override
    public void render(TileEntityAdvancedSolarGenerator tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AdvancedSolarGenerator.png"));
        MekanismRenderer.rotate(tileEntity.getDirection(), 0, 180, 90, 270);
        GlStateManager.rotatef(180, 0, 0, 1);
        model.render(0.0625F);
        GlStateManager.popMatrix();
    }
}