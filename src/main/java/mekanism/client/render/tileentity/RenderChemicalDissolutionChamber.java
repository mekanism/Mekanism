package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderChemicalDissolutionChamber extends MekanismTileEntityRenderer<TileEntityChemicalDissolutionChamber> {

    private ModelChemicalDissolutionChamber model = new ModelChemicalDissolutionChamber();

    @Override
    public void func_225616_a_(@Nonnull TileEntityChemicalDissolutionChamber tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "chemical_dissolution_chamber.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        RenderSystem.rotatef(180, 0, 0, 1);
        model.render(0.0625F);
        RenderSystem.popMatrix();*/
    }
}