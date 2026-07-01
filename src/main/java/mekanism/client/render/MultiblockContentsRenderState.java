package mekanism.client.render;

import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.util.LightLevelUtils;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class MultiblockContentsRenderState extends BlockEntityRenderState {

    public BlockPos renderLocation = BlockPos.ZERO;
    public int length, width, height;

    public void calculateLightCoords(@Nullable Level level, MultiblockData multiblock) {
        calculateLightCoords(level, multiblock, 0);
    }

    public void calculateLightCoords(@Nullable Level level, MultiblockData multiblock, int emission) {
        if (emission == Level.MAX_BRIGHTNESS) {
            //If it is already fully emissive like lava, skip looking up the light coords of the level
            lightCoords = LightCoordsUtil.FULL_BRIGHT;
        } else if (level != null) {//Shouldn't really be null, but given the base impl for BlockEntityState supports checking if it is, just let it handle it
            BlockPos minPos = multiblock.getMinPos();
            BlockPos maxPos = multiblock.getMaxPos();
            //Note: We offset the min position and max position by one so that we are not counting the frame. That way we only take into account light that is affected
            // by things like structural glass. We know this is valid positioning wise, due to the sanity checks in MultiblockTileEntityRenderer#extractRenderState.
            // This also has the added benefit of allowing us to check fewer positions
            int lightLevel = LightLevelUtils.getMaxLightCoordsBounds(level, minPos.offset(1, 1, 1), maxPos.offset(-1, -1, -1));
            int blockLight = LightCoordsUtil.block(lightLevel);
            if (blockLight > 0 && blockLight < Level.MAX_BRIGHTNESS) {
                //Note: If there is a block light level, and it is not at max brightness, that means that we have block light making it into the multiblock
                // presumably through a block like structural glass. So we need to increase the block light by one so that we have the light level at the structural glass
                // instead of one inside it
                lightLevel = LightCoordsUtil.withBlock(lightLevel, blockLight + 1);
            }
            lightCoords = LightCoordsUtil.lightCoordsWithEmission(lightLevel, emission);
        }
    }
}
