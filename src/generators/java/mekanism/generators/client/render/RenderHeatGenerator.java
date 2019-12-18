package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelHeatGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderHeatGenerator extends MekanismTileEntityRenderer<TileEntityHeatGenerator> {

    private ModelHeatGenerator model = new ModelHeatGenerator();

    @Override
    public void func_225616_a_(@Nonnull TileEntityHeatGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        field_228858_b_.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "heat_generator.png"));

        MekanismRenderer.rotate(tile.getDirection(), 180, 0, 270, 90);

        RenderSystem.rotatef(180, 0, 0, 1);
        model.render(0.0625F, tile.getActive(), field_228858_b_.textureManager);
        RenderSystem.popMatrix();*/
    }
}