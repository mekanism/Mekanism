package mekanism.generators.client.render;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTurbineRotor extends TileEntitySpecialRenderer<TileEntityTurbineRotor> {

    private static final float BASE_SPEED = 512F;
    public static boolean internalRender = false;
    private ModelTurbine model = new ModelTurbine();

    @Override
    public void render(TileEntityTurbineRotor tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        renderAModelAt(tileEntity, x, y, z, partialTick);
    }

    private void renderAModelAt(TileEntityTurbineRotor tileEntity, double x, double y, double z, float partialTick) {
        if (tileEntity.getMultiblock() != null && !internalRender) {
            return;
        }

        GlStateManager.pushMatrix();
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Turbine.png"));

        int baseIndex = tileEntity.getPosition() * 2;
        float rotateSpeed = 0.0F;

        if (tileEntity.getMultiblock() != null && SynchronizedTurbineData.clientRotationMap.containsKey(tileEntity.getMultiblock())) {
            rotateSpeed = SynchronizedTurbineData.clientRotationMap.get(tileEntity.getMultiblock());
        }

        if (!Mekanism.proxy.isPaused()) {
            tileEntity.rotationLower = (tileEntity.rotationLower + rotateSpeed * BASE_SPEED * (1F / (float) (baseIndex + 1))) % 360;
            tileEntity.rotationUpper = (tileEntity.rotationUpper + rotateSpeed * BASE_SPEED * (1F / (float) (baseIndex + 2))) % 360;
        }

        if (tileEntity.getHousedBlades() > 0) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5F, (float) y - 1F, (float) z + 0.5F);
            GlStateManager.rotate(tileEntity.rotationLower, 0, 1, 0);
            model.render(0.0625F, baseIndex);
            GlStateManager.popMatrix();
        }

        if (tileEntity.getHousedBlades() == 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5F, (float) y - 0.5F, (float) z + 0.5F);
            GlStateManager.rotate(tileEntity.rotationUpper, 0, 1, 0);
            model.render(0.0625F, baseIndex + 1);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }
}