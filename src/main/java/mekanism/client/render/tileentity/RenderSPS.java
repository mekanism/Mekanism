package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import javax.annotation.ParametersAreNonnullByDefault;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@ParametersAreNonnullByDefault
public class RenderSPS extends MekanismTileEntityRenderer<TileEntitySPSCasing> {

    private static final CustomEffect CORE = new CustomEffect(MekanismUtils.getResource(ResourceType.RENDER, "energy_effect.png"));
    private static final Map<UUID, BoltRenderer> boltRendererMap = new HashMap<>();
    private static final float MIN_SCALE = 0.1F, MAX_SCALE = 4F;
    private static final Random rand = new Random();

    public static void clearBoltRenderers() {
        boltRendererMap.clear();
    }

    private final Minecraft minecraft = Minecraft.getInstance();

    public RenderSPS(BlockEntityRendererProvider.Context context) {
        super(context);
        CORE.setColor(Color.rgbai(255, 255, 255, 240));
    }

    @Override
    protected void render(TileEntitySPSCasing tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        if (tile.isMaster()) {
            SPSMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed() && multiblock.renderLocation != null && multiblock.getBounds() != null) {
                BoltRenderer bolts = boltRendererMap.computeIfAbsent(multiblock.inventoryID, mb -> new BoltRenderer());
                Vec3 center = Vec3.atLowerCornerOf(multiblock.getMinPos()).add(Vec3.atLowerCornerOf(multiblock.getMaxPos()))
                      .add(new Vec3(1, 1, 1)).scale(0.5);
                Vec3 renderCenter = center.subtract(tile.getBlockPos().getX(), tile.getBlockPos().getY(), tile.getBlockPos().getZ());
                if (!minecraft.isPaused()) {
                    for (CoilData data : multiblock.coilData.coilMap.values()) {
                        if (data.prevLevel > 0) {
                            bolts.update(data.coilPos.hashCode(), getBoltFromData(data, tile.getBlockPos(), multiblock, renderCenter), partialTick);
                        }
                    }
                }

                float energyScale = getEnergyScale(multiblock.lastProcessed);
                int targetEffectCount = 0;

                if (!minecraft.isPaused() && !multiblock.lastReceivedEnergy.isZero()) {
                    if (rand.nextDouble() < getBoundedScale(energyScale, 0.01F, 0.4F)) {
                        CuboidSide side = CuboidSide.SIDES[rand.nextInt(6)];
                        Plane plane = Plane.getInnerCuboidPlane(multiblock.getBounds(), side);
                        Vec3 endPos = plane.getRandomPoint(rand).subtract(tile.getBlockPos().getX(), tile.getBlockPos().getY(), tile.getBlockPos().getZ());
                        BoltEffect bolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, renderCenter, endPos, 15)
                              .size(0.01F * getBoundedScale(energyScale, 0.5F, 5))
                              .lifespan(8)
                              .spawn(SpawnFunction.NO_DELAY);
                        bolts.update(Objects.hash(side, endPos), bolt, partialTick);
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
                    CORE.setPos(center);
                    CORE.setScale(getBoundedScale(energyScale, MIN_SCALE, MAX_SCALE));
                    BillboardingEffectRenderer.render(CORE, tile.getBlockPos(), matrix, renderer, tile.getLevel().getGameTime(), partialTick);
                }

                tile.orbitEffects.forEach(effect -> BillboardingEffectRenderer.render(effect, tile.getBlockPos(), matrix, renderer, tile.getLevel().getGameTime(), partialTick));
            }
        }
    }

    private static float getEnergyScale(double lastProcessed) {
        return (float) Math.min(1, Math.max(0, (Math.log10(lastProcessed) + 2) / 4D));
    }

    private static float getBoundedScale(float scale, float min, float max) {
        return min + scale * (max - min);
    }

    private static BoltEffect getBoltFromData(CoilData data, BlockPos pos, SPSMultiblockData multiblock, Vec3 center) {
        Vec3 start = Vec3.atCenterOf(data.coilPos.relative(data.side));
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
    public boolean shouldRenderOffScreen(TileEntitySPSCasing tile) {
        if (tile.isMaster()) {
            SPSMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && multiblock.renderLocation != null;
        }
        return false;
    }
}
