package mekanism.generators.client.render;

import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderReactor extends TileEntitySpecialRenderer<TileEntityReactorController> {

    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityReactorController tileEntity, double x, double y, double z, float partialTick,
          int destroyStage, float alpha) {
        if (tileEntity.isBurning()) {
            GlStateManager.pushMatrix();
            GL11.glTranslated(x + 0.5, y - 1.5, z + 0.5);
            bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png"));

            MekanismRenderer.blendOn();
            MekanismRenderer.glowOn();

            EnumColor c;
            double scale;
            long scaledTemp = Math.round(tileEntity.getPlasmaTemp() / 1E8);

            c = EnumColor.AQUA;

            GlStateManager.pushMatrix();
            scale = 1 + 0.7 * Math
                  .sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 3.14 * scaledTemp + 135F));
            GL11.glScaled(scale, scale, scale);
            GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), 1);
            GlStateManager.rotate((MekanismClient.ticksPassed + partialTick) * -6 * scaledTemp, 0, 1, 0);
            GlStateManager.rotate(36F + (MekanismClient.ticksPassed + partialTick) * -7 * scaledTemp, 0, 1, 1);
            core.render(0.0625F);
            GlStateManager.popMatrix();

            c = EnumColor.RED;

            GlStateManager.pushMatrix();
            scale = 1 + 0.8 * Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 3 * scaledTemp));
            GL11.glScaled(scale, scale, scale);
            GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), 1);
            GlStateManager.rotate((MekanismClient.ticksPassed + partialTick) * 4 * scaledTemp, 0, 1, 0);
            GlStateManager.rotate(36F + (MekanismClient.ticksPassed + partialTick) * 4 * scaledTemp, 0, 1, 1);
            core.render(0.0625F);
            GlStateManager.popMatrix();

            c = EnumColor.ORANGE;

            GlStateManager.pushMatrix();
            scale =
                  1 - 0.9 * Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 4 * scaledTemp + 90F));
            GL11.glScaled(scale, scale, scale);
            GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), 1);
            GlStateManager.rotate((MekanismClient.ticksPassed + partialTick) * 5 * scaledTemp - 35F, 0, 1, 0);
            GlStateManager.rotate(36F + (MekanismClient.ticksPassed + partialTick) * -3 * scaledTemp + 70F, 0, 1, 1);
            core.render(0.0625F);
            GlStateManager.popMatrix();

            MekanismRenderer.glowOff();
            MekanismRenderer.blendOff();
            MekanismRenderer.resetColor();

            GlStateManager.popMatrix();
        }
    }
}
