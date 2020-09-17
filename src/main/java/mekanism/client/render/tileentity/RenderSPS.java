package mekanism.client.render.tileentity;

import com.google.common.base.Objects;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.HashMap;
import java.util.Map;
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
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

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

    public RenderSPS(TileEntityRendererDispatcher renderer) {
        super(renderer);
        CORE.setColor(Color.rgbai(255, 255, 255, 240));
    }

    @Override
    protected void render(TileEntitySPSCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.isMaster) {
            SPSMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed() && multiblock.renderLocation != null && multiblock.getBounds() != null) {
                BoltRenderer bolts = boltRendererMap.computeIfAbsent(multiblock.inventoryID, mb -> new BoltRenderer());
                Vector3d center = Vector3d.copy(multiblock.getMinPos()).add(Vector3d.copy(multiblock.getMaxPos())).add(new Vector3d(1, 1, 1)).scale(0.5);
                Vector3d renderCenter = center.subtract(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
                if (!minecraft.isGamePaused()) {
                    for (CoilData data : multiblock.coilData.coilMap.values()) {
                        if (data.prevLevel > 0) {
                            bolts.update(data.coilPos.hashCode(), getBoltFromData(data, tile.getPos(), multiblock, renderCenter), partialTick);
                        }
                    }
                }

                float energyScale = getEnergyScale(multiblock.lastProcessed);
                int targetEffectCount = 0;

                if (!minecraft.isGamePaused() && !multiblock.lastReceivedEnergy.isZero()) {
                    if (rand.nextDouble() < getBoundedScale(energyScale, 0.01F, 0.4F)) {
                        CuboidSide side = CuboidSide.SIDES[rand.nextInt(6)];
                        Plane plane = Plane.getInnerCuboidPlane(multiblock.getBounds(), side);
                        Vector3d endPos = plane.getRandomPoint(rand).subtract(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
                        BoltEffect bolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, renderCenter, endPos, 15)
                              .size(0.01F * getBoundedScale(energyScale, 0.5F, 5))
                              .lifespan(8)
                              .spawn(SpawnFunction.NO_DELAY);
                        bolts.update(Objects.hashCode(side.hashCode(), endPos.hashCode()), bolt, partialTick);
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
                    BillboardingEffectRenderer.render(CORE, tile.getPos(), matrix, renderer, tile.getWorld().getGameTime(), partialTick);
                }

                tile.orbitEffects.forEach(effect -> BillboardingEffectRenderer.render(effect, tile.getPos(), matrix, renderer, tile.getWorld().getGameTime(), partialTick));
            }
        }
    }

    private static float getEnergyScale(double lastProcessed) {
        return (float) Math.min(1, Math.max(0, (Math.log10(lastProcessed) + 2) / 4D));
    }

    private static float getBoundedScale(float scale, float min, float max) {
        return min + scale * (max - min);
    }

    private static BoltEffect getBoltFromData(CoilData data, BlockPos pos, SPSMultiblockData multiblock, Vector3d center) {
        Vector3d start = Vector3d.copyCentered(data.coilPos.offset(data.side));
        start = start.add(Vector3d.copy(data.side.getDirectionVec()).scale(0.5));
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
    public boolean isGlobalRenderer(TileEntitySPSCasing tile) {
        if (tile.isMaster) {
            SPSMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && multiblock.renderLocation != null;
        }
        return false;
    }
}
