package mekanism.client.render.tileentity;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tier.EnergyCubeTier;
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

    public static Map<EnergyCubeTier, ResourceLocation> resources = new EnumMap<>(EnergyCubeTier.class);
    public static ResourceLocation baseTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube.png");
    public static ResourceLocation coreTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png");

    static {
        if (resources.isEmpty()) {
            for (EnergyCubeTier tier : EnergyCubeTier.values()) {
                resources.put(tier, MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube" + tier.getBaseTier().getSimpleName() + ".png"));
            }
        }
    }

    private ModelEnergyCube model = new ModelEnergyCube();
    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityEnergyCube tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);

        bindTexture(baseTexture);

        switch (tileEntity.facing.ordinal()) {
            case 0: {
                GlStateManager.rotate(90F, -1.0F, 0.0F, 0.0F);
                renderHelper.translateYZ(1.0F, -1.0F);
                break;
            }
            case 1: {
                GlStateManager.rotate(90F, 1.0F, 0.0F, 0.0F);
                renderHelper.translateYZ(1.0F, 1.0F);
                break;
            }
            case 2:
                GlStateManager.rotate(0, 0.0F, 1.0F, 0.0F);
                break;
            case 3:
                GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
                break;
            case 4:
                GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
                break;
            case 5:
                GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F);
                break;
        }

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        model.render(0.0625F, tileEntity.tier, rendererDispatcher.renderEngine, false);

        for (EnumFacing side : EnumFacing.values()) {
            bindTexture(baseTexture);
            model.renderSide(0.0625F, side, tileEntity.configComponent.getOutput(TransmissionType.ENERGY, side).ioState, tileEntity.tier, rendererDispatcher.renderEngine);
        }

        renderHelper.cleanup();

        if (tileEntity.getEnergy() / tileEntity.getMaxEnergy() > 0.1) {
            MekanismRenderHelper coreRenderHelper = new MekanismRenderHelper(true);
            coreRenderHelper.translate(x + 0.5, y + 0.5, z + 0.5);
            bindTexture(coreTexture);
            coreRenderHelper.enableBlendPreset().enableGlow();

            MekanismRenderHelper coreColorRenderHelper = new MekanismRenderHelper(true).scale(0.4F).color(tileEntity.tier.getBaseTier());
            coreColorRenderHelper.translateY(Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 3)) / 7);
            GlStateManager.rotate((MekanismClient.ticksPassed + partialTick) * 4, 0, 1, 0);
            GlStateManager.rotate(36F + (MekanismClient.ticksPassed + partialTick) * 4, 0, 1, 1);
            core.render(0.0625F);
            coreColorRenderHelper.cleanup();
            coreRenderHelper.cleanup();
        }

        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage, alpha);
    }
}