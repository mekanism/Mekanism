package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.Mekanism;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;

public class RenderTurbineRotor extends MekanismTileEntityRenderer<TileEntityTurbineRotor> {

    private static final float BASE_SPEED = 512F;
    public static boolean internalRender = false;
    private ModelTurbine model = new ModelTurbine();

    @Override
    public void func_225616_a_(@Nonnull TileEntityTurbineRotor tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        if (tile.getMultiblock() != null && !internalRender) {
            return;
        }

        matrix.func_227860_a_();

        int baseIndex = tile.getPosition() * 2;
        float rotateSpeed = 0.0F;

        if (tile.getMultiblock() != null && SynchronizedTurbineData.clientRotationMap.containsKey(tile.getMultiblock())) {
            rotateSpeed = SynchronizedTurbineData.clientRotationMap.get(tile.getMultiblock());
        }

        if (!Mekanism.proxy.isPaused()) {
            tile.rotationLower = (tile.rotationLower + rotateSpeed * BASE_SPEED * (1F / (float) (baseIndex + 1))) % 360;
            tile.rotationUpper = (tile.rotationUpper + rotateSpeed * BASE_SPEED * (1F / (float) (baseIndex + 2))) % 360;
        }

        if (tile.getHousedBlades() > 0) {
            matrix.func_227860_a_();
            matrix.func_227861_a_(0.5, -1, 0.5);
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(tile.rotationLower));
            model.render(matrix, renderer, light, otherLight, baseIndex);
            matrix.func_227865_b_();
        }

        if (tile.getHousedBlades() == 2) {
            matrix.func_227860_a_();
            matrix.func_227861_a_(0.5, -0.5, 0.5);
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(tile.rotationUpper));
            model.render(matrix, renderer, light, otherLight, baseIndex + 1);
            matrix.func_227865_b_();
        }
        matrix.func_227865_b_();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineRotor tile) {
        return tile.getMultiblock() != null && !internalRender;
    }
}