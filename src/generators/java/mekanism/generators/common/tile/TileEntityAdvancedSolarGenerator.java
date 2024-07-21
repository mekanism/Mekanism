package mekanism.generators.common.tile;

import mekanism.api.IEvaporationSolar;
import mekanism.api.RelativeSide;
import mekanism.api.math.MathUtils;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityAdvancedSolarGenerator extends TileEntitySolarGenerator implements IBoundingBlock, IEvaporationSolar {

    private static final RelativeSide[] ENERGY_SIDES = {RelativeSide.FRONT, RelativeSide.BOTTOM};
    private final SolarCheck[] solarChecks = new SolarCheck[8];

    public TileEntityAdvancedSolarGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, pos, state, MekanismGeneratorsConfig.generators.advancedSolarGeneration);
    }

    @Override
    protected RelativeSide[] getEnergySides() {
        return ENERGY_SIDES;
    }

    @Override
    protected long getConfiguredMax() {
        return MekanismGeneratorsConfig.generators.advancedSolarGeneration.get();
    }

    @Override
    protected void recheckSettings() {
        if (level == null) {
            return;
        }
        BlockPos topPos = worldPosition.above(2);
        solarCheck = new AdvancedSolarCheck(level, topPos);
        float totalPeak = solarCheck.getPeakMultiplier();
        for (int i = 0; i < solarChecks.length; i++) {
            if (i < 3) {
                solarChecks[i] = new AdvancedSolarCheck(level, topPos.offset(-1, 0, i - 1));
            } else if (i == 3) {
                solarChecks[i] = new AdvancedSolarCheck(level, topPos.offset(0, 0, -1));
            } else if (i == 4) {
                solarChecks[i] = new AdvancedSolarCheck(level, topPos.offset(0, 0, 1));
            } else {
                solarChecks[i] = new AdvancedSolarCheck(level, topPos.offset(1, 0, i - 6));
            }
            totalPeak += solarChecks[i].getPeakMultiplier();
        }
        updateMaxOutputRaw(MathUtils.clampToLong(getConfiguredMax() * (totalPeak / 9)));
    }

    @Override
    protected boolean checkCanSeeSun() {
        if (solarCheck == null) {
            //Note: We assume if solarCheck is null then solarChecks will be filled with null, and if it isn't
            // then it won't be as they get initialized at the same time
            return false;
        }
        //Allow attempting to recheck each position, and mark that we can see the sun if at least one position can
        solarCheck.recheckCanSeeSun();
        byte count = solarCheck.canSeeSun() ? (byte) 1 : 0;
        for (SolarCheck check : solarChecks) {
            check.recheckCanSeeSun();
            if (check.canSeeSun()) {
                count++;
            }
        }
        //Mark that our solar generator can "see" the sun if at least five of the nine positions
        // are able to see the sun
        return count > 4;
    }

    @Override
    public long getProduction() {
        if (level == null || solarCheck == null) {
            //Note: We assume if solarCheck is null then solarChecks will be filled with null, and if it isn't
            // then it won't be as they get initialized at the same time
            return 0;
        }
        float brightness = getBrightnessMultiplier(level);
        //Calculate the generation multiplier of all the solar panels together
        // any part that can't see the sun will contribute zero to the multiplier,
        // and then we take the average across all to see how much to multiply by
        float generationMultiplier = solarCheck.getGenerationMultiplier();
        for (SolarCheck check : solarChecks) {
            generationMultiplier += check.getGenerationMultiplier();
        }
        generationMultiplier /= solarChecks.length + 1;
        //Production is a function of the peak possible output in this biome and sun's current brightness
        return MathUtils.clampToLong(getConfiguredMax() * (brightness * generationMultiplier));
    }

    private static class AdvancedSolarCheck extends SolarCheck {

        private final int recheckFrequency;
        private long lastCheckedSun;

        public AdvancedSolarCheck(Level world, BlockPos pos) {
            super(world, pos);
            //Recheck between every 10-30 ticks, to not end up checking each position each tick
            recheckFrequency = Mth.nextInt(world.random, MekanismUtils.TICKS_PER_HALF_SECOND, MekanismUtils.TICKS_PER_HALF_SECOND + SharedConstants.TICKS_PER_SECOND);
        }

        @Override
        public void recheckCanSeeSun() {
            if (!world.dimensionType().hasSkyLight() || world.getSkyDarken() >= 4) {
                //Inline of most of WorldUtils#canSeeSun so that we can exit early if it is not day or there is no skylight
                // We start with the basic dimension checks and always run those, as they are simple and quick checks, and
                // we want to be able to stop quickly when it gets too dark
                canSeeSun = false;
                return;
            }
            long time = world.getGameTime();
            if (time < lastCheckedSun + recheckFrequency) {
                //If we have checked for blocks above the solar panel in the past recheckFrequency
                // number of ticks, skip checking for now for performance reasons
                return;
            }
            // otherwise, mark that we checked and actually check
            lastCheckedSun = time;
            if (world.getFluidState(pos).isEmpty()) {
                //If the top isn't fluid logged we can just quickly check if the top can see the sun
                canSeeSun = world.canSeeSky(pos);
            } else {
                BlockPos above = pos.above();
                if (world.canSeeSky(above)) {
                    //If the spot above can see the sun, check to make sure we can see through the block there
                    BlockState state = world.getBlockState(above);
                    canSeeSun = !state.liquid() && state.getLightBlock(world, above) <= 0;
                } else {
                    canSeeSun = false;
                }
            }
        }
    }
}