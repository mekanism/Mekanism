package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelGasGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderGasGenerator extends MekanismTileEntityRenderer<TileEntityGasGenerator> {

    private ModelGasGenerator model = new ModelGasGenerator();

    @Override
    public void func_225616_a_(@Nonnull TileEntityGasGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        field_228858_b_.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "gas_burning_generator.png"));

        MekanismRenderer.rotate(tile.getDirection(), 90, 270, 180, 0);

        RenderSystem.rotatef(180, 0, 1, 1);
        RenderSystem.rotatef(90, -1, 0, 0);
        RenderSystem.rotatef(90, 0, 1, 0);
        model.render(0.0625F);
        RenderSystem.popMatrix();*/
    }
}