package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelGasGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderGasGenerator extends MekanismTileEntityRenderer<TileEntityGasGenerator> {

    private ModelGasGenerator model = new ModelGasGenerator();

    @Override
    public void func_225616_a_(@Nonnull TileEntityGasGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "gas_burning_generator.png"));

        MekanismRenderer.rotate(tile.getDirection(), 90, 270, 180, 0);

        GlStateManager.rotatef(180, 0, 1, 1);
        GlStateManager.rotatef(90, -1, 0, 0);
        GlStateManager.rotatef(90, 0, 1, 0);
        model.render(0.0625F);
        GlStateManager.popMatrix();
    }
}