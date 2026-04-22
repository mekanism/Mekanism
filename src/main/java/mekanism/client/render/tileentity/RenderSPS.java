package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.tileentity.RenderSPS.SPSRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData.CoilData;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.lib.effect.CustomEffect;
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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderSPS extends MultiblockTileEntityRenderer<SPSMultiblockData, TileEntitySPSCasing, SPSRenderState> {

    private static final CustomEffect CORE = Util.make(new CustomEffect(MekanismUtils.getResource(ResourceType.RENDER, "energy_effect.png")),
          core -> core.setColor(Color.rgbai(255, 255, 255, 240)));
    private static final Map<UUID, BoltRenderer> boltRendererMap = new HashMap<>();
    private static final float MIN_SCALE = 0.1F, MAX_SCALE = 4F;
    private static final RandomSource rand = RandomSource.create();

    public static void clearBoltRenderers() {
        boltRendererMap.clear();
    }

    public RenderSPS(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SPSRenderState createRenderState() {
        return new SPSRenderState();
    }

    @Override
    public void extractRenderState(TileEntitySPSCasing sps, SPSRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(sps, state, partialTick, cameraPosition, breakProgress);
        SPSMultiblockData multiblock = sps.getMultiblock();
        state.setProcessed(multiblock.lastProcessed);

        state.center = Vec3.atLowerCornerOf(multiblock.getMinPos())
              .add(Vec3.atLowerCornerOf(multiblock.getMaxPos()))
              .add(1, 1, 1)
              .scale(0.5);

        BlockPos pos = state.blockPos;
        BoltRenderer bolts = boltRendererMap.computeIfAbsent(multiblock.inventoryID, mb -> new BoltRenderer());

        int targetEffectCount = 0;
        boolean tickingNormally = isTickingNormally(sps);
        if (tickingNormally) {
            Vec3 renderCenter = state.center.subtract(pos.getX(), pos.getY(), pos.getZ());
            for (CoilData data : multiblock.coilData.coilMap.values()) {
                if (data.prevLevel > 0) {
                    bolts.update(data.coilPos.hashCode(), getBoltFromData(data, pos, renderCenter), partialTick);
                }
            }
            if (multiblock.lastReceivedEnergy > 0L) {
                if (rand.nextDouble() < state.lerpEnergy(0.01F, 0.4F)) {
                    CuboidSide side = Util.getRandom(CuboidSide.SIDES, rand);
                    Plane plane = Plane.getInnerCuboidPlane(multiblock.getBounds(), side);
                    Vec3 endPos = plane.getRandomPoint(rand).subtract(pos.getX(), pos.getY(), pos.getZ());
                    BoltEffect bolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, renderCenter, endPos, 15)
                          .size(0.01F * state.lerpEnergy(0.5F, 5))
                          .lifespan(8)
                          .spawn(SpawnFunction.NO_DELAY);
                    bolts.update(31 * side.hashCode() + endPos.hashCode(), bolt, partialTick);
                }
                targetEffectCount = (int) state.lerpEnergy(10, 120);
            }
        }

        if (sps.orbitEffects.size() > targetEffectCount) {
            sps.orbitEffects.poll();
        } else if (sps.orbitEffects.size() < targetEffectCount && rand.nextDouble() < 0.5) {
            //TODO - 26.1: Do we want to just use the level's random instead?
            sps.orbitEffects.add(new SPSOrbitEffect(multiblock, state.center));
        }
    }

    @Override
    public void submit(SPSRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        //TODO - 26.1: Figure out how to render these things, should they potentially use the CustomGeometryRenderer thing?
        // I think the billboarding effects might be able to use a model part, which then might let them be ordered properly in terms of transparency
        //bolts.render(partialTick, poseStack, renderer);

        poseStack.pushPose();
        //Because our center points are the center of the multiblock, we translate back to 0,0,0
        //TODO - 26.1: Test this is the proper way to handle that we used to shift the billboarding effect renderer by the camera position
        // when we were batching rendering of billboarding effects
        poseStack.translate(-state.blockPos.getX(), -state.blockPos.getY(), -state.blockPos.getZ());

        if (state.processed > 0 && state.center != null) {
            //TODO - 26.1: ProfilerConstants.SPS_CORE ?
            CORE.setPos(state.center);
            CORE.setScale(state.lerpEnergy(MIN_SCALE, MAX_SCALE));
            //BillboardingEffectRenderer.render(CORE, camera, renderer, poseStack, renderTick, partialTick);
        }

        //TODO - 26.1: ProfilerConstants.SPS_ORBIT ?
        /*for (SPSOrbitEffect effect : sps.orbitEffects) {
            BillboardingEffectRenderer.render(effect, camera, renderer, poseStack, renderTick, partialTick);
        }*/
        poseStack.popPose();
    }

    private static BoltEffect getBoltFromData(CoilData data, BlockPos pos, Vec3 center) {
        Vec3 start = data.coilPos.relative(data.side).getCenter();
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

        @Nullable
        public Vec3 center;
        public double processed;
        public float energyScale;

        public void setProcessed(double processed) {
            this.processed = processed;
            this.energyScale = Mth.clamp((float) (Math.log10(processed) + 2) / 4, 0, 1);
        }

        public float lerpEnergy(float min, float max) {
            return Mth.lerp(energyScale, min, max);
        }
    }
}
