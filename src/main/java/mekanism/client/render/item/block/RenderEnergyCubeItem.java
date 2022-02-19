package mekanism.client.render.item.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class RenderEnergyCubeItem extends ItemStackTileEntityRenderer {

    private static final ModelEnergyCube energyCube = new ModelEnergyCube();
    private static final ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        EnergyCubeTier tier = ((ItemBlockEnergyCube) stack.getItem()).getTier();
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrix.pushPose();
        matrix.translate(0, -1, 0);
        //TODO: Instead of having this be a thing, make it do it from model like the block does?
        energyCube.render(matrix, renderer, light, overlayLight, tier, true, stack.hasFoil());
        energyCube.renderSidesBatched(stack, tier, matrix, renderer, light, overlayLight, stack.hasFoil());
        matrix.popPose();
        double energyPercentage = StorageUtils.getStoredEnergyFromNBT(stack).divideToLevel(tier.getMaxEnergy());
        if (energyPercentage > 0) {
            matrix.scale(0.4F, 0.4F, 0.4F);
            matrix.translate(0, Math.sin(Math.toRadians(3 * MekanismClient.ticksPassed)) / 7, 0);
            matrix.mulPose(Vector3f.YP.rotationDegrees(4 * MekanismClient.ticksPassed));
            matrix.mulPose(RenderEnergyCube.coreVec.rotationDegrees(36F + 4 * MekanismClient.ticksPassed));
            core.render(matrix, renderer, MekanismRenderer.FULL_LIGHT, overlayLight, tier.getBaseTier().getColor(), (float) energyPercentage);
        }
        matrix.popPose();
    }
}