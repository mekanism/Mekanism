package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelChemicalDissolutionChamber;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderChemicalDissolutionChamber extends MekanismTileEntityRenderer<TileEntityChemicalDissolutionChamber> {

    private ModelChemicalDissolutionChamber model = new ModelChemicalDissolutionChamber();

    @Override
    public void func_225616_a_(@Nonnull TileEntityChemicalDissolutionChamber tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        model.render(matrix, renderer, light, otherLight);
        matrix.func_227865_b_();
    }
}