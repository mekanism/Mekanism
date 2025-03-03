package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.api.RelativeSide;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.MekanismISTER;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.IPersistentConfigInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class RenderEnergyCubeItem extends MekanismISTER {

    public static final RenderEnergyCubeItem RENDERER = new RenderEnergyCubeItem();
    private ModelEnergyCore core;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        core = new ModelEnergyCore(getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {
        EnergyCubeTier tier = ((ItemBlockEnergyCube) stack.getItem()).getTier();
        CubeSideState[] sideStates = new CubeSideState[EnumUtils.SIDES.length];
        AttachedSideConfig fallback = tier == EnergyCubeTier.CREATIVE ? ItemBlockEnergyCube.ALL_OUTPUT : ItemBlockEnergyCube.SIDE_CONFIG;
        IPersistentConfigInfo sideConfig = AttachedSideConfig.getStoredConfigInfo(stack, fallback, TransmissionType.ENERGY);
        for (RelativeSide side : EnumUtils.SIDES) {
            DataType dataType = sideConfig.getDataType(side);
            CubeSideState state = CubeSideState.INACTIVE;
            if (dataType != DataType.NONE) {
                state = dataType.canOutput() ? CubeSideState.ACTIVE_LIT : CubeSideState.ACTIVE_UNLIT;
            }
            sideStates[side.ordinal()] = state;
        }
        ModelData modelData = ModelData.of(TileEntityEnergyCube.SIDE_STATE_PROPERTY, sideStates);
        renderBlockItem(stack, displayContext, matrix, renderer, light, overlayLight, modelData);
        double energyPercentage = StorageUtils.getEnergyRatio(stack);
        if (energyPercentage > 0) {
            float ticks = Minecraft.getInstance().levelRenderer.getTicks() + MekanismRenderer.getPartialTick();
            float scaledTicks = 4 * ticks;
            matrix.pushPose();
            matrix.translate(0.5, 0.5, 0.5);
            matrix.scale(0.4F, 0.4F, 0.4F);
            matrix.translate(0, Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
            matrix.mulPose(Axis.YP.rotationDegrees(scaledTicks));
            matrix.mulPose(RenderEnergyCube.coreVec.rotationDegrees(36F + scaledTicks));
            core.render(matrix, renderer, LightTexture.FULL_BRIGHT, overlayLight, tier.getBaseTier(), (float) energyPercentage);
            matrix.popPose();
        }
    }
}