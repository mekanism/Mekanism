package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
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
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderEnergyCube extends TileEntitySpecialRenderer<TileEntityEnergyCube> {

    public static int[][] COLORS = new int[][]{new int[]{100, 210, 125}, new int[]{215, 85, 70},
          new int[]{80, 125, 230},
          new int[]{154, 120, 200}, new int[]{0, 0, 0}};
    public static Map<EnergyCubeTier, ResourceLocation> resources = new HashMap<>();
    public static ResourceLocation baseTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube.png");
    public static ResourceLocation coreTexture = MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png");

    static {
        if (resources.isEmpty()) {
            for (EnergyCubeTier tier : EnergyCubeTier.values()) {
                resources.put(tier, MekanismUtils
                      .getResource(ResourceType.RENDER, "EnergyCube" + tier.getBaseTier().getSimpleName() + ".png"));
            }
        }
    }

    private ModelEnergyCube model = new ModelEnergyCube();
    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityEnergyCube tileEntity, double x, double y, double z, float partialTick,
          int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

        bindTexture(baseTexture);

        switch (tileEntity.facing.ordinal()) {
            case 0: {
                GlStateManager.rotate(90F, -1.0F, 0.0F, 0.0F);
                GlStateManager.translate(0.0F, 1.0F, -1.0F);
                break;
            }
            case 1: {
                GlStateManager.rotate(90F, 1.0F, 0.0F, 0.0F);
                GlStateManager.translate(0.0F, 1.0F, 1.0F);
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
            model.renderSide(0.0625F, side, tileEntity.configComponent.getOutput(TransmissionType.ENERGY, side).ioState,
                  tileEntity.tier, rendererDispatcher.renderEngine);
        }

        GlStateManager.popMatrix();

        if (tileEntity.getEnergy() / tileEntity.getMaxEnergy() > 0.1) {
            GlStateManager.pushMatrix();
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            bindTexture(coreTexture);

            MekanismRenderer.blendOn();
            MekanismRenderer.glowOn();

            int[] c = COLORS[tileEntity.tier.getBaseTier().ordinal()];

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.4F, 0.4F, 0.4F);
            GL11.glColor4f((float) c[0] / 255F, (float) c[1] / 255F, (float) c[2] / 255F,
                  (float) (tileEntity.getEnergy() / tileEntity.getMaxEnergy()));
            GlStateManager
                  .translate(0, (float) Math.sin(Math.toRadians((MekanismClient.ticksPassed + partialTick) * 3)) / 7,
                        0);
            GlStateManager.rotate((MekanismClient.ticksPassed + partialTick) * 4, 0, 1, 0);
            GlStateManager.rotate(36F + (MekanismClient.ticksPassed + partialTick) * 4, 0, 1, 1);
            core.render(0.0625F);
            MekanismRenderer.resetColor();
            GlStateManager.popMatrix();

            MekanismRenderer.glowOff();
            MekanismRenderer.blendOff();

            GlStateManager.popMatrix();
        }

        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage, alpha);
    }
}
