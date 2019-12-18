package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderAdvancedSolarGenerator extends MekanismTileEntityRenderer<TileEntityAdvancedSolarGenerator> {

    private ModelAdvancedSolarGenerator model = new ModelAdvancedSolarGenerator();

    @Override
    public void func_225616_a_(@Nonnull TileEntityAdvancedSolarGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        field_228858_b_.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "advanced_solar_generator.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        RenderSystem.rotatef(180, 0, 0, 1);
        model.render(0.0625F);
        RenderSystem.popMatrix();*/
    }

    @Override
    public boolean isGlobalRenderer(TileEntityAdvancedSolarGenerator tile) {
        return true;
    }
}