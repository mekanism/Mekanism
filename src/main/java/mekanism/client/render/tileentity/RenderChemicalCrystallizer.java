package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderChemicalCrystallizer extends MekanismTileEntityRenderer<TileEntityChemicalCrystallizer> {

    private ModelChemicalCrystallizer model = new ModelChemicalCrystallizer();

    @Override
    public void func_225616_a_(@Nonnull TileEntityChemicalCrystallizer tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "chemical_crystallizer.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        RenderSystem.rotatef(180, 0, 0, 1);
        model.render(0.0625F);
        RenderSystem.popMatrix();
        MekanismRenderer.machineRenderer().func_225616_a_(tile, partialTick, matrix, renderer, light, otherLight);*/
    }
}