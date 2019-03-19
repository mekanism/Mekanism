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
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderTurbineRotor extends TileEntitySpecialRenderer<TileEntityTurbineRotor> {

    private static final float BASE_SPEED = 512F;
    public static boolean internalRender = false;
    private ModelTurbine model = new ModelTurbine();

    @Override
    public void render(TileEntityTurbineRotor tileEntity, double x, double y, double z, float partialTick,
          int destroyStage, float alpha) {
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

        if (tileEntity.getMultiblock() != null && SynchronizedTurbineData.clientRotationMap
              .containsKey(tileEntity.getMultiblock())) {
            rotateSpeed = SynchronizedTurbineData.clientRotationMap.get(tileEntity.getMultiblock());
        }

        if (!Mekanism.proxy.isPaused()) {
            tileEntity.rotationLower =
                  (tileEntity.rotationLower + rotateSpeed * BASE_SPEED * (1F / (float) (baseIndex + 1))) % 360;
            tileEntity.rotationUpper =
                  (tileEntity.rotationUpper + rotateSpeed * BASE_SPEED * (1F / (float) (baseIndex + 2))) % 360;
        }

        if (tileEntity.getHousedBlades() > 0) {
            GlStateManager.pushMatrix();
            GL11.glTranslated(x + 0.5, y - 1, z + 0.5);
            GlStateManager.rotate(tileEntity.rotationLower, 0.0F, 1.0F, 0.0F);
            model.render(0.0625F, baseIndex);
            GlStateManager.popMatrix();
        }

        if (tileEntity.getHousedBlades() == 2) {
            GlStateManager.pushMatrix();
            GL11.glTranslated(x + 0.5, y - 0.5, z + 0.5);
            GlStateManager.rotate(tileEntity.rotationUpper, 0.0F, 1.0F, 0.0F);
            model.render(0.0625F, baseIndex + 1);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }
}
