package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import mekanism.api.RelativeSide;
import mekanism.api.tier.BaseTier;
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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RenderEnergyCubeItem extends MekanismISTER<RenderEnergyCubeItem.CubeState> {

    public static final RenderEnergyCubeItem RENDERER = new RenderEnergyCubeItem();
    private final ModelEnergyCore core = new ModelEnergyCore(getEntityModels());

    @Override
    public void submit(@Nullable CubeState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) {
            return;
        }
        //todo 26.1 rendering
        //renderBlockItem(stack, displayContext, matrix, renderer, light, overlayLight, modelData);
        if (state.energyRatio > 0) {
            float scaledTicks = 4 * state.ticks();
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.translate(0, Math.sin(Math.toRadians(3 * state.ticks())) / 7, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(scaledTicks));
            poseStack.mulPose(RenderEnergyCube.coreVec.rotationDegrees(36F + scaledTicks));
            core.collect(ModelEnergyCore.getState(state.baseTier, state.energyRatio), poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, overlayCoords, false);
            poseStack.popPose();
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        //TODO 26.1 getExtents
    }

    @Nullable
    @Override
    public CubeState extractArgument(ItemStack stack) {
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
        float ticks = Minecraft.getInstance().levelRenderer.getTicks() + MekanismRenderer.getPartialTick();
        float energyRatio = (float) StorageUtils.getEnergyRatio(stack);
        return new CubeState(modelData, tier.getBaseTier(), energyRatio, ticks, stack.hasFoil());
    }

    public record CubeState(ModelData blockData, BaseTier baseTier, float energyRatio, float ticks, boolean hasFoil) {}
}