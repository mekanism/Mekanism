package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderTurbineRotor extends MekanismTileEntityRenderer<TileEntityTurbineRotor> {

    private static final float BASE_SPEED = 512F;
    public static boolean internalRender = false;
    private ModelTurbine model = new ModelTurbine();

    @Override
    public void func_225616_a_(@Nonnull TileEntityTurbineRotor tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        //renderAModelAt(tile, x, y, z, partialTick);
    }

    private void renderAModelAt(TileEntityTurbineRotor tile, double x, double y, double z, float partialTick) {
        //TODO: 1.15
        /*if (tile.getMultiblock() != null && !internalRender) {
            return;
        }

        RenderSystem.pushMatrix();
        field_228858_b_.textureManager.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "turbine.png"));

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
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) x + 0.5F, (float) y - 1F, (float) z + 0.5F);
            RenderSystem.rotatef(tile.rotationLower, 0, 1, 0);
            model.render(0.0625F, baseIndex);
            RenderSystem.popMatrix();
        }

        if (tile.getHousedBlades() == 2) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) x + 0.5F, (float) y - 0.5F, (float) z + 0.5F);
            RenderSystem.rotatef(tile.rotationUpper, 0, 1, 0);
            model.render(0.0625F, baseIndex + 1);
            RenderSystem.popMatrix();
        }

        RenderSystem.popMatrix();*/
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineRotor tile) {
        return tile.getMultiblock() != null && !internalRender;
    }
}