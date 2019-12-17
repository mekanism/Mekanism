package mekanism.client.render.item.block;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import org.lwjgl.opengl.GL11;

public class RenderEnergyCubeItem extends MekanismItemStackRenderer {

    private static ModelEnergyCube energyCube = new ModelEnergyCube();
    private static ModelEnergyCore core = new ModelEnergyCore();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        EnergyCubeTier tier = ((ItemBlockEnergyCube) stack.getItem()).getTier(stack);
        if (tier == null) {
            return;
        }
        RenderSystem.pushMatrix();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.pushMatrix();
        RenderSystem.translatef(0, -1.0F, 0);
        MekanismRenderer.bindTexture(RenderEnergyCube.baseTexture);
        energyCube.render(0.0625F, tier, Minecraft.getInstance().textureManager, true);

        for (Direction side : EnumUtils.DIRECTIONS) {
            MekanismRenderer.bindTexture(RenderEnergyCube.baseTexture);
            energyCube.renderSide(0.0625F, side, true, side == Direction.NORTH, Minecraft.getInstance().textureManager);
        }
        RenderSystem.popMatrix();

        double energyPercentage = ItemDataUtils.getDouble(stack, "energyStored") / tier.getMaxEnergy();
        if (energyPercentage > 0.1) {
            MekanismRenderer.bindTexture(RenderEnergyCube.coreTexture);
            GlowInfo glowInfo = MekanismRenderer.enableGlow();
            RenderSystem.scalef(0.4F, 0.4F, 0.4F);
            MekanismRenderer.color(tier.getBaseTier().getColor(), (float) energyPercentage);
            RenderSystem.translatef(0, (float) Math.sin(Math.toRadians(3 * MekanismClient.ticksPassed)) / 7, 0);
            RenderSystem.rotatef(4 * MekanismClient.ticksPassed, 0, 1, 0);
            RenderSystem.rotatef(36F + 4 * MekanismClient.ticksPassed, 0, 1, 1);
            core.render(0.0625F);
            MekanismRenderer.resetColor();
            MekanismRenderer.disableGlow(glowInfo);
        }

        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.popMatrix();
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {

    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}