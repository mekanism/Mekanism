package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.lib.effect.BillboardingEffectFeatureRenderer;
import mekanism.client.render.lib.effect.BillboardingEffectFeatureRenderer.BillboardingRenderState;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.tileentity.RenderSPS.SPSRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData.CoilData;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.lib.math.Plane;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import mekanism.common.particle.SPSOrbitEffect;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;
import org.jspecify.annotations.Nullable;

public class RenderSPS extends MultiblockTileEntityRenderer<SPSMultiblockData, TileEntitySPSCasing, SPSRenderState> {

    private static final RenderType CORE_RENDER_TYPE = MekanismRenderType.SPS.apply(MekanismUtils.getResource(ResourceType.RENDER, "energy_effect.png"));
    private static final Map<UUID, BoltRenderer> boltRendererMap = new HashMap<>();

    public static void clearBoltRenderers() {
        boltRendererMap.clear();
    }

    private final RandomSource random = RandomSource.create();

    public RenderSPS(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SPSRenderState createRenderState() {
        return new SPSRenderState();
    }

    @Override
    public void extractRenderState(TileEntitySPSCasing sps, SPSRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(sps, state, partialTick, cameraPosition, breakProgress);
        SPSMultiblockData multiblock = sps.getMultiblock();
        state.setProcessed(multiblock.lastProcessed);

        state.center = Vec3.atLowerCornerOf(multiblock.getMinPos())
              .add(Vec3.atLowerCornerOf(multiblock.getMaxPos()))
              .add(1, 1, 1)
              .scale(0.5);

        BlockPos pos = state.blockPos;
        BoltRenderer bolts = boltRendererMap.computeIfAbsent(multiblock.inventoryID, _ -> new BoltRenderer());

        int targetEffectCount = 0;
        boolean tickingNormally = isTickingNormally(sps);
        if (tickingNormally) {
            Vec3 renderCenter = state.center.subtract(pos.getX(), pos.getY(), pos.getZ());
            for (CoilData data : multiblock.coilData.coilMap.values()) {
                if (data.prevLevel > 0) {
                    bolts.update(data.coilPos.hashCode(), getBoltFromData(data, pos, renderCenter), partialTick);
                }
            }
            if (multiblock.lastReceivedEnergy > 0) {
                if (random.nextDouble() < state.lerpEnergy(0.01F, 0.4F)) {
                    CuboidSide side = Util.getRandom(CuboidSide.SIDES, random);
                    Plane plane = Plane.getInnerCuboidPlane(multiblock.getBounds(), side);
                    Vec3 endPos = plane.getRandomPoint(random).subtract(pos.getX(), pos.getY(), pos.getZ());
                    BoltEffect bolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, renderCenter, endPos, 15)
                          .size(0.01F * state.lerpEnergy(0.5F, 5))
                          .lifespan(8)
                          .spawn(SpawnFunction.NO_DELAY);
                    bolts.update(31 * side.hashCode() + endPos.hashCode(), bolt, partialTick);
                }
                targetEffectCount = (int) state.lerpEnergy(10, 120);
            }
        }

        if (state.processed > 0) {
            state.coreState = new BillboardingRenderState();
            state.coreState.color = 0xF0FFFFFF;
            state.coreState.gridSize = 4;
            state.coreState.scale = state.lerpEnergy(0.1F, 4F);
            //TODO - 26.2: Re-evaluate this, ConduitBlockEntity#tickCount?
            state.coreState.renderTick =  (int) sps.getGameTime();
        }

        if (sps.orbitEffects.size() > targetEffectCount) {
            sps.orbitEffects.poll();
        } else if (sps.orbitEffects.size() < targetEffectCount && random.nextDouble() < 0.5) {
            sps.orbitEffects.add(new SPSOrbitEffect(multiblock, random));
        }
        for (SPSOrbitEffect effect : sps.orbitEffects) {
            BillboardingRenderState orbitState = new BillboardingRenderState();
            orbitState.color = SPSOrbitEffect.COLOR;
            orbitState.scale = effect.getScale();
            orbitState.renderTick = effect.getTick();
            effect.transformPos(orbitState.pos, partialTick);
            state.orbitEffects.add(orbitState);
        }
    }

    @Override
    public void submit(SPSRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        //TODO - 26.2: Figure out how to render these things, should they potentially use the CustomGeometryRenderer thing?
        // I think the billboarding effects might be able to use a model part, which then might let them be ordered properly in terms of transparency
        //bolts.render(partialTick, poseStack, renderer);

        if (state.center != null) {
            poseStack.pushPose();
            //Render from the center position instead of from the block's position
            poseStack.translate(state.center.x() - state.blockPos.getX(), state.center.y() - state.blockPos.getY(), state.center.z() - state.blockPos.getZ());
            poseStack.mulPose(camera.orientation);

            if (state.coreState != null) {
                submitBillboard(poseStack, nodeCollector, CORE_RENDER_TYPE, state.coreState);
            }

            for (BillboardingRenderState orbitEffect : state.orbitEffects) {
                submitBillboard(poseStack, nodeCollector, SPSOrbitEffect.RENDER_TYPE, orbitEffect);
            }
            poseStack.popPose();
        }
    }

    private void submitBillboard(PoseStack poseStack, SubmitNodeCollector nodeCollector, RenderType renderType, BillboardingRenderState state) {
        poseStack.pushPose();
        poseStack.translate(state.pos.x(), state.pos.y(), state.pos.z());
        poseStack.scale(state.scale, state.scale, state.scale);
        nodeCollector.submitSpecial(RenderPhaseKeys.AFTER_TERRAIN, new BillboardingEffectFeatureRenderer.Submit(poseStack.last().copy(), renderType, state));
        poseStack.popPose();
    }

    private static BoltEffect getBoltFromData(CoilData data, BlockPos pos, Vec3 center) {
        Vec3 start = Vec3.atCenterOf(data.coilPos.relative(data.side));
        start = start.add(Vec3.atLowerCornerOf(data.side.getUnitVec3i()).scale(0.5));
        int count = 1 + (data.prevLevel - 1) / 2;
        float size = 0.01F * data.prevLevel;
        return new BoltEffect(BoltRenderInfo.ELECTRICITY, start.subtract(pos.getX(), pos.getY(), pos.getZ()), center, 15)
              .count(count).size(size).lifespan(8).spawn(SpawnFunction.delay(4));
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SPS;
    }

    @Override
    protected boolean shouldRender(TileEntitySPSCasing tile, SPSMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && multiblock.getBounds() != null;
    }

    public static class SPSRenderState extends BlockEntityRenderState {

        public final List<BillboardingRenderState> orbitEffects = new ArrayList<>();
        @Nullable
        public Vec3 center;
        public double processed;
        public float energyScale;
        @Nullable
        public BillboardingRenderState coreState;

        public void setProcessed(double processed) {
            this.processed = processed;
            this.energyScale = Math.clamp((float) (Math.log10(processed) + 2) / 4, 0, 1);
        }

        public float lerpEnergy(float min, float max) {
            return Mth.lerp(energyScale, min, max);
        }
    }
}
