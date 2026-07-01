package mekanism.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class LightLevelUtils {

    /// Gets the skylight factor, a percentage based on the time of day and weather. Vanilla max of 1
    ///
    /// @return percentage value of float type. Normal range of 0 - 1. CAN BE HIGHER
    public static float getSunBrightness(Level level, BlockPos position) {
        return Math.max(0, level.environmentAttributes().getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, position));
    }

    /// Checks to see if the block at the position can see the sky, and it is daytime.
    ///
    /// @param level Level to check in.
    /// @param pos   Position to check.
    ///
    /// @return `true` if it can.
    @Contract("null, _ -> false")
    public static boolean canSeeSun(@Nullable Level level, BlockPos pos) {
        //Note: We manually handle the level#isDaytime check by just checking the subtracted skylight
        // as vanilla returns false if the level's time is set to a fixed value even if that time
        // would effectively be daytime
        return level != null && level.dimensionType().hasSkyLight() && level.getSkyDarken() < 4 && level.canSeeSky(pos);
    }

    /// Calculates the max light level along the surface of the bounds, ignoring [net.minecraft.world.level.block.state.BlockState#emissiveRendering()] and
    /// [net.neoforged.neoforge.common.extensions.IBlockStateExtension#getLightEmission] as our multiblocks are not made up of blocks that give off light.
    public static int getMaxLightCoordsBounds(Level level, BlockPos minPos, BlockPos maxPos) {
        //TODO - 26.2: Evaluate the performance of this and potentially try to do something like Flywheel where we read from the raw light arrays for a single section
        // https://github.com/Engine-Room/Flywheel/blob/26.1.2/dev/common/src/backend/java/dev/engine_room/flywheel/backend/engine/LightDataCollector.java
        //Similar to BrightnessGetter.DEFAULT, but only looks up the layers once
        LevelLightEngine lightEngine = level.getLightEngine();
        LayerLightEventListener skyLightLayer = lightEngine.getLayerListener(LightLayer.SKY);
        LayerLightEventListener blockLightLayer = lightEngine.getLayerListener(LightLayer.BLOCK);
        boolean hasSkyLayer = skyLightLayer != LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
        boolean hasBlockLayer = blockLightLayer != LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
        if (!hasSkyLayer && !hasBlockLayer) {
            return 0;
        }
        LightLevel lightLevel = new LightLevel();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = maxPos.getY(); y >= minPos.getY(); y--) {
            boolean isBorderY = y == maxPos.getY() || y == minPos.getY();
            for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
                if (isBorderY || x == minPos.getX() || x == maxPos.getX()) {
                    for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                        pos.set(x, y, z);
                        if (lightLevel.updateBlockLight(blockLightLayer, pos, hasSkyLayer) || lightLevel.updateSkyLight(skyLightLayer, pos, hasSkyLayer)) {
                            break;
                        }
                    }
                } else {
                    pos.set(x, y, minPos.getZ());
                    if (lightLevel.updateBlockLight(blockLightLayer, pos, hasSkyLayer) || lightLevel.updateSkyLight(skyLightLayer, pos, hasSkyLayer)) {
                        break;
                    }
                    pos.set(x, y, maxPos.getZ());
                    if (lightLevel.updateBlockLight(blockLightLayer, pos, hasSkyLayer) || lightLevel.updateSkyLight(skyLightLayer, pos, hasSkyLayer)) {
                        break;
                    }
                }
            }
        }
        return lightLevel.getLightCoords();
    }

    private static class LightLevel {

        private int maxBlock;
        private int maxSky;

        public int getLightCoords() {
            return LightCoordsUtil.pack(maxBlock, maxSky);
        }

        public boolean updateBlockLight(LayerLightEventListener lightLayer, BlockPos pos, boolean hasSkyLayer) {
            int block = lightLayer.getLightValue(pos);
            if (block > maxBlock) {
                maxBlock = block;
                if (maxBlock == Level.MAX_BRIGHTNESS) {
                    return !hasSkyLayer || maxSky == Level.MAX_BRIGHTNESS;
                }
            }
            return false;
        }

        public boolean updateSkyLight(LayerLightEventListener lightLayer, BlockPos pos, boolean hasBlockLayer) {
            int sky = lightLayer.getLightValue(pos);
            if (sky > maxSky) {
                maxSky = sky;
                if (maxSky == Level.MAX_BRIGHTNESS) {
                    return !hasBlockLayer || maxBlock == Level.MAX_BRIGHTNESS;
                }
            }
            return false;
        }
    }
}