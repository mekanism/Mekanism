package mekanism.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

//TODO: Fix being able to see through the energy cube into the void if standing on top of one that is on the ground
@OnlyIn(Dist.CLIENT)
public class RenderEnergyCube extends TileEntityRenderer<TileEntityEnergyCube> {

    public static ResourceLocation baseTexture = MekanismUtils.getResource(ResourceType.RENDER, "energy_cube.png");
    public static ResourceLocation coreTexture = MekanismUtils.getResource(ResourceType.RENDER, "energy_core.png");

    private ModelEnergyCube model = new ModelEnergyCube();
    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityEnergyCube tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        //TODO: Debate converting the energy cube to a normal baked model and then just have this draw the model AND then add the core in the middle
        // Would this improve performance at all? We probably would have to put port state information into the blockstate
        GlStateManager.pushMatrix();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

        GlStateManager.pushMatrix();
        switch (tileEntity.getDirection()) {
            case DOWN:
                GlStateManager.rotatef(90, -1, 0, 0);
                GlStateManager.translatef(0, 1.0F, -1.0F);
                break;
            case UP:
                GlStateManager.rotatef(90, 1, 0, 0);
                GlStateManager.translatef(0, 1.0F, 1.0F);
                break;
            default:
                //Otherwise use the helper method for handling different face options because it is one of them
                MekanismRenderer.rotate(tileEntity.getDirection(), 0, 180, 90, 270);
                break;
        }

        GlStateManager.rotatef(180, 0, 0, 1);
        model.render(0.0625F, tileEntity.tier, rendererDispatcher.textureManager, false);

        setLightmapDisabled(true);
        for (Direction side : Direction.values()) {
            bindTexture(baseTexture);
            model.renderSide(0.0625F, side, tileEntity.configComponent.getOutput(TransmissionType.ENERGY, side).ioState, rendererDispatcher.textureManager);
        }
        setLightmapDisabled(false);
        GlStateManager.popMatrix();

        double energyPercentage = tileEntity.getEnergy() / tileEntity.getMaxEnergy();
        if (energyPercentage > 0.1) {
            GlStateManager.translatef(0, -1.0F, 0);
            bindTexture(coreTexture);
            GlowInfo glowInfo = MekanismRenderer.enableGlow();
            float ticks = MekanismClient.ticksPassed + partialTick;
            GlStateManager.scalef(0.4F, 0.4F, 0.4F);
            MekanismRenderer.color(tileEntity.tier.getBaseTier().getColor(), (float) energyPercentage);
            GlStateManager.translatef(0, (float) Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
            GlStateManager.rotatef(4 * ticks, 0, 1, 0);
            GlStateManager.rotatef(36F + 4 * ticks, 0, 1, 1);
            core.render(0.0625F);
            MekanismRenderer.resetColor();
            MekanismRenderer.disableGlow(glowInfo);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.popMatrix();
        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage);
    }
}