package mekanism.generators.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class RenderReactor extends TileEntityRenderer<TileEntityReactorController> {

    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityReactorController tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        if (tileEntity.isBurning()) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) x + 0.5F, (float) y - 1.5F, (float) z + 0.5F);
            bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png"));

            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableAlphaTest();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlowInfo glowInfo = MekanismRenderer.enableGlow();

            long scaledTemp = Math.round(tileEntity.getPlasmaTemp() / 1E8);
            float ticks = MekanismClient.ticksPassed + partialTick;
            double scale = 1 + 0.7 * Math.sin(Math.toRadians(ticks * 3.14 * scaledTemp + 135F));
            renderPart(EnumColor.AQUA, scale, ticks, scaledTemp, -6, -7, 0, 36);

            scale = 1 + 0.8 * Math.sin(Math.toRadians(ticks * 3 * scaledTemp));
            renderPart(EnumColor.RED, scale, ticks, scaledTemp, 4, 4, 0, 36);

            scale = 1 - 0.9 * Math.sin(Math.toRadians(ticks * 4 * scaledTemp + 90F));
            renderPart(EnumColor.ORANGE, scale, ticks, scaledTemp, 5, -3, -35, 106);

            MekanismRenderer.disableGlow(glowInfo);
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.popMatrix();
        }
    }

    private void renderPart(EnumColor color, double scale, float ticks, long scaledTemp, int mult1, int mult2, int shift1, int shift2) {
        float ticksScaledTemp = ticks * scaledTemp;
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) scale, (float) scale, (float) scale);
        MekanismRenderer.color(color);
        GlStateManager.rotatef(ticksScaledTemp * mult1 + shift1, 0, 1, 0);
        GlStateManager.rotatef(ticksScaledTemp * mult2 + shift2, 0, 1, 1);
        core.render(0.0625F);
        MekanismRenderer.resetColor();
        GlStateManager.popMatrix();
    }
}