package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;

public class RenderReactor extends MekanismTileEntityRenderer<TileEntityReactorController> {

    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void func_225616_a_(@Nonnull TileEntityReactorController tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int otherLight) {
        if (tile.isBurning()) {
            matrix.func_227860_a_();
            matrix.func_227861_a_(0.5, -1.5, 0.5);

            long scaledTemp = Math.round(tile.getPlasmaTemp() / 1E8);
            float ticks = MekanismClient.ticksPassed + partialTick;
            double scale = 1 + 0.7 * Math.sin(Math.toRadians(ticks * 3.14 * scaledTemp + 135F));
            renderPart(matrix, renderer, otherLight, EnumColor.AQUA, scale, ticks, scaledTemp, -6, -7, 0, 36);

            scale = 1 + 0.8 * Math.sin(Math.toRadians(ticks * 3 * scaledTemp));
            renderPart(matrix, renderer, otherLight, EnumColor.RED, scale, ticks, scaledTemp, 4, 4, 0, 36);

            scale = 1 - 0.9 * Math.sin(Math.toRadians(ticks * 4 * scaledTemp + 90F));
            renderPart(matrix, renderer, otherLight, EnumColor.ORANGE, scale, ticks, scaledTemp, 5, -3, -35, 106);

            matrix.func_227865_b_();
        }
    }

    private void renderPart(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int otherLight, EnumColor color, double scale, float ticks,
          long scaledTemp, int mult1, int mult2, int shift1, int shift2) {
        float ticksScaledTemp = ticks * scaledTemp;
        matrix.func_227860_a_();
        matrix.func_227862_a_((float) scale, (float) scale, (float) scale);
        matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(ticksScaledTemp * mult1 + shift1));
        matrix.func_227863_a_(RenderEnergyCube.coreVec.func_229187_a_(ticksScaledTemp * mult2 + shift2));
        core.render(matrix, renderer, MekanismRenderer.FULL_LIGHT, otherLight, color, 1);
        matrix.func_227865_b_();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityReactorController tile) {
        return tile.isBurning();
    }
}