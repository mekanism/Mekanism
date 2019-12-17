package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityGasTank;
import net.minecraft.client.renderer.IRenderTypeBuffer;

//TODO: Just directly tell GasTank it is rendering via RenderConfigurableMachine?
public class RenderGasTank extends MekanismTileEntityRenderer<TileEntityGasTank> {

    @Override
    public void func_225616_a_(@Nonnull TileEntityGasTank tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        MekanismRenderer.machineRenderer().func_225616_a_(tile, partialTick, matrix, renderer, light, otherLight);
    }
}