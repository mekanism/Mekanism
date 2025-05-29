package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.effect.BillboardingEffectRenderer;
import mekanism.client.render.lib.effect.BoltRenderer;
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
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderSPS extends MultiblockTileEntityRenderer<SPSMultiblockData, TileEntitySPSCasing> {

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
    protected void render(TileEntitySPSCasing tile, SPSMultiblockData multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light,
          int overlayLight, ProfilerFiller profiler) {
        BoltRenderer bolts = boltRendererMap.computeIfAbsent(multiblock.inventoryID, mb -> new BoltRenderer());
        Vec3 center = Vec3.atLowerCornerOf(multiblock.getMinPos()).add(Vec3.atLowerCornerOf(multiblock.getMaxPos()))
              .add(new Vec3(1, 1, 1)).scale(0.5);
        Vec3 renderCenter = center.subtract(tile.getBlockPos().getX(), tile.getBlockPos().getY(), tile.getBlockPos().getZ());
        boolean tickingNormally = isTickingNormally(tile);
        if (tickingNormally) {
            for (CoilData data : multiblock.coilData.coilMap.values()) {
                if (data.prevLevel > 0) {
                    bolts.update(data.coilPos.hashCode(), getBoltFromData(data, tile.getBlockPos(), renderCenter), partialTick);
                }
            }
        }

        float energyScale = getEnergyScale(multiblock.lastProcessed);
        int targetEffectCount = 0;

        if (tickingNormally && multiblock.lastReceivedEnergy > 0L) {
            if (rand.nextDouble() < getBoundedScale(energyScale, 0.01F, 0.4F)) {
                CuboidSide side = Util.getRandom(CuboidSide.SIDES, rand);
                Plane plane = Plane.getInnerCuboidPlane(multiblock.getBounds(), side);
                Vec3 endPos = plane.getRandomPoint(rand).subtract(tile.getBlockPos().getX(), tile.getBlockPos().getY(), tile.getBlockPos().getZ());
                BoltEffect bolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, renderCenter, endPos, 15)
                      .size(0.01F * getBoundedScale(energyScale, 0.5F, 5))
                      .lifespan(8)
                      .spawn(SpawnFunction.NO_DELAY);
                bolts.update(31 * side.hashCode() + endPos.hashCode(), bolt, partialTick);
            }
            targetEffectCount = (int) getBoundedScale(energyScale, 10, 120);
        }

        if (tile.orbitEffects.size() > targetEffectCount) {
            tile.orbitEffects.poll();
        } else if (tile.orbitEffects.size() < targetEffectCount && rand.nextDouble() < 0.5) {
            tile.orbitEffects.add(new SPSOrbitEffect(multiblock, center));
        }

        bolts.render(partialTick, matrix, renderer);

        if (multiblock.lastProcessed > 0) {
            float scale = getBoundedScale(energyScale, MIN_SCALE, MAX_SCALE);
            BillboardingEffectRenderer.render(CORE.getTexture(), ProfilerConstants.SPS_CORE, () -> {
                //Lazily update the position and stuff, so it gets set just before rendering
                CORE.setPos(center);
                CORE.setScale(scale);
                return CORE;
            });
        }

        for (SPSOrbitEffect effect : tile.orbitEffects) {
            BillboardingEffectRenderer.render(effect, ProfilerConstants.SPS_ORBIT);
        }
    }

    private static float getEnergyScale(double lastProcessed) {
        return (float) Math.min(1, Math.max(0, (Math.log10(lastProcessed) + 2) / 4D));
    }

    private static float getBoundedScale(float scale, float min, float max) {
        return min + scale * (max - min);
    }

    private static BoltEffect getBoltFromData(CoilData data, BlockPos pos, Vec3 center) {
        Vec3 start = data.coilPos.relative(data.side).getCenter();
        start = start.add(Vec3.atLowerCornerOf(data.side.getNormal()).scale(0.5));
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
}
