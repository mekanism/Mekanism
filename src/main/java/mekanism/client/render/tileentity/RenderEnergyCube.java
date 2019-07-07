package mekanism.client.render.tileentity;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnergyCube extends TileEntitySpecialRenderer<TileEntityEnergyCube> {

    public static ResourceLocation baseTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube.png");
    public static ResourceLocation coreTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png");

    private ModelEnergyCube model = new ModelEnergyCube();
    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityEnergyCube tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).enableBlendPreset();
        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        switch (tileEntity.facing) {
            case DOWN:
                GlStateManager.rotate(90, -1, 0, 0);
                GlStateManager.translate(0, 1.0F, -1.0F);
                break;
            case UP:
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.translate(0, 1.0F, 1.0F);
                break;
            case NORTH:
                GlStateManager.rotate(0, 0, 1, 0);
                break;
            case SOUTH:
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.rotate(270, 0, 1, 0);
                break;
        }

        GlStateManager.rotate(180, 0, 0, 1);
        model.render(0.0625F, tileEntity.tier, rendererDispatcher.renderEngine, false);

        for (EnumFacing side : EnumFacing.values()) {
            bindTexture(baseTexture);
            model.renderSide(0.0625F, side, tileEntity.configComponent.getOutput(TransmissionType.ENERGY, side).ioState, tileEntity.tier, rendererDispatcher.renderEngine);
        }

        renderHelper.cleanup();

        if (tileEntity.getEnergy() / tileEntity.getMaxEnergy() > 0.1) {
            MekanismRenderHelper coreRenderHelper = new MekanismRenderHelper(true);
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            bindTexture(coreTexture);
            coreRenderHelper.enableBlendPreset().enableGlow();

            float ticks = MekanismClient.ticksPassed + partialTick;
            MekanismRenderHelper coreColorRenderHelper = new MekanismRenderHelper(true);
            GlStateManager.scale(0.4F, 0.4F, 0.4F);
            coreColorRenderHelper.color(tileEntity.tier.getBaseTier());
            GlStateManager.translate(0, Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
            GlStateManager.rotate(4 * ticks, 0, 1, 0);
            GlStateManager.rotate(36F + 4 * ticks, 0, 1, 1);
            core.render(0.0625F);
            coreColorRenderHelper.cleanup();
            coreRenderHelper.cleanup();
        }

        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage, alpha);
    }
}