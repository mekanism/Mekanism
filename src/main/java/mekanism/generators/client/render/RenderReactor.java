package mekanism.generators.client.render;

import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderReactor extends TileEntitySpecialRenderer<TileEntityReactorController> {

    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityReactorController tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (tileEntity.isBurning()) {
            MekanismRenderHelper renderHelper = new MekanismRenderHelper(true);
            GlStateManager.translate(x + 0.5, y - 1.5, z + 0.5);
            bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png"));

            MekanismRenderer.blendOn(renderHelper);
            MekanismRenderer.glowOn();

            long scaledTemp = Math.round(tileEntity.getPlasmaTemp() / 1E8);
            float ticks = MekanismClient.ticksPassed + partialTick;
            double scale = 1 + 0.7 * Math.sin(Math.toRadians(ticks * 3.14 * scaledTemp + 135F));
            renderPart(EnumColor.AQUA, scale, ticks, scaledTemp, -6, -7, 0, 36);

            scale = 1 + 0.8 * Math.sin(Math.toRadians(ticks * 3 * scaledTemp));
            renderPart(EnumColor.RED, scale, ticks, scaledTemp, 4, 4, 0, 36);

            scale = 1 - 0.9 * Math.sin(Math.toRadians(ticks * 4 * scaledTemp + 90F));
            renderPart(EnumColor.ORANGE, scale, ticks, scaledTemp, 5, -3, -35, 106);

            MekanismRenderer.glowOff();
            renderHelper.cleanup();
        }
    }

    private void renderPart(EnumColor color, double scale, float ticks, long scaledTemp, int mult1, int mult2, int shift1, int shift2) {
        float ticksScaledTemp = ticks * scaledTemp;
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).scale(scale).color(color);
        GlStateManager.rotate(ticksScaledTemp * mult1 + shift1, 0, 1, 0);
        GlStateManager.rotate(ticksScaledTemp * mult2 + shift2, 0, 1, 1);
        core.render(0.0625F);
        renderHelper.cleanup();
    }
}