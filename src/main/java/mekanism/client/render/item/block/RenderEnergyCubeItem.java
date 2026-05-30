package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.RelativeSide;
import mekanism.client.ModelUtil;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.model.blockstate.EnergyCubeModel;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube.CubeSideState;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.IPersistentConfigInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RenderEnergyCubeItem implements SpecialModelRenderer<RenderEnergyCubeItem.CubeState> {
    
    private final ModelEnergyCore core;
    private final Lazy<Vector3fc[]> extents = Lazy.of(() -> ModelUtil.computeExtents(MekanismBlocks.CREATIVE_ENERGY_CUBE));

    public RenderEnergyCubeItem(EntityModelSet entityModels) {
        core = new ModelEnergyCore(entityModels);
    }

    @Override
    public void submit(@Nullable CubeState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) {
            return;
        }
        state.blockRenderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        if (state.coreState != null) {
            float scaledTicks = 4 * state.ticks();
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(0.4F, 0.4F, 0.4F);
            poseStack.translate(0, Math.sin(Math.toRadians(3 * state.ticks())) / 7, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(scaledTicks));
            poseStack.mulPose(RenderEnergyCube.coreVec.rotationDegrees(36F + scaledTicks));
            core.collect(state.coreState, poseStack, submitNodeCollector, LightCoordsUtil.FULL_BRIGHT, overlayCoords, false);
            poseStack.popPose();
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (Vector3fc vector3fc : extents.get()) {
            output.accept(vector3fc);
        }
    }

    @Nullable
    @Override
    public CubeState extractArgument(ItemStack stack) {
        ItemBlockEnergyCube itemBlock = (ItemBlockEnergyCube) stack.getItem();
        EnergyCubeTier tier = itemBlock.getTier();
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

        BlockModelRenderState modelRenderState = new BlockModelRenderState();
        BlockState blockState = itemBlock.getBlock().defaultBlockState();
        BlockStateModel blockStateModel = models().getBlockStateModelSet().get(blockState);
        if (blockStateModel instanceof EnergyCubeModel energyCubeModel) {
            List<BlockStateModelPart> partList = modelRenderState.setupModel(ModelUtil.IDENTITY, (energyCubeModel.materialFlags() & BakedQuad.FLAG_TRANSLUCENT) != 0);
            energyCubeModel.collectParts(partList, sideStates);
            modelRenderState.tintLayers().add(tier.getBaseTier().getPackedColor());
        } else {
            //weird, but ok, try to render something
            mc().getBlockModelResolver().update(modelRenderState, blockState, ModelUtil.BLOCK_DISPLAY_NO_CONTEXT);
        }

        float ticks = mc().levelRenderer.getTicks() + MekanismRenderer.getPartialTick();
        float energyRatio = (float) StorageUtils.getEnergyRatio(stack);

        return new CubeState(
              energyRatio > 0 ? ModelEnergyCore.getState(tier.getBaseTier(), energyRatio) : null,
              ticks,
              stack.hasFoil(),
              modelRenderState
        );
    }

    private static ModelManager models() {
        return mc().getModelManager();
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public record CubeState(@Nullable Integer coreState, float ticks, boolean hasFoil, BlockModelRenderState blockRenderState) {}

    public static class Unbaked implements SpecialModelRenderer.Unbaked<CubeState> {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        @Nullable
        public SpecialModelRenderer<CubeState> bake(BakingContext context) {
            return new RenderEnergyCubeItem(context.entityModelSet());
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<CubeState>> type() {
            return MAP_CODEC;
        }
    }
}