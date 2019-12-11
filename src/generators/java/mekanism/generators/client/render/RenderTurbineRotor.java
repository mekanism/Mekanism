package mekanism.generators.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class RenderTurbineRotor extends TileEntityRenderer<TileEntityTurbineRotor> {

    private static final float BASE_SPEED = 512F;
    public static boolean internalRender = false;
    private ModelTurbine model = new ModelTurbine();

    @Override
    public void render(TileEntityTurbineRotor tile, double x, double y, double z, float partialTick, int destroyStage) {
        renderAModelAt(tile, x, y, z, partialTick);
    }

    private void renderAModelAt(TileEntityTurbineRotor tile, double x, double y, double z, float partialTick) {
        if (tile.getMultiblock() != null && !internalRender) {
            return;
        }

        GlStateManager.pushMatrix();
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "turbine.png"));

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
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.5F, (float) y - 1F, (float) z + 0.5F);
            GlStateManager.rotatef(tile.rotationLower, 0, 1, 0);
            model.render(0.0625F, baseIndex);
            GlStateManager.popMatrix();
        }

        if (tile.getHousedBlades() == 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.5F, (float) y - 0.5F, (float) z + 0.5F);
            GlStateManager.rotatef(tile.rotationUpper, 0, 1, 0);
            model.render(0.0625F, baseIndex + 1);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineRotor tile) {
        return tile.getMultiblock() != null && !internalRender;
    }
}