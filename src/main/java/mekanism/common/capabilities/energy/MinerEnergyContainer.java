package mekanism.common.capabilities.energy;

import mekanism.api.IContentsListener;
import mekanism.api.math.MathUtils;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class MinerEnergyContainer extends MachineEnergyContainer<TileEntityDigitalMiner> {

    public MinerEnergyContainer(TileEntityDigitalMiner tile, @Nullable IContentsListener listener) {
        super(tile, listener);
    }

    @Override
    protected int calculateEnergyPerTick(int baseEnergyPerTick, Holder.@Nullable Reference<Upgrade> speedUpgrade, Holder.@Nullable Reference<Upgrade> energyUpgrade) {
        int energyPerTick = super.calculateEnergyPerTick(baseEnergyPerTick, speedUpgrade, energyUpgrade);
        return calculateEnergyPerTick(energyPerTick, tile.getLevel(), tile.getSilkTouch(), tile.getRadius(), tile.getMinY(), tile.getMaxY());
    }

    public static int calculateEnergyPerTick(int energyPerTick, @Nullable Level level, boolean silkTouch, int radius, int minY, int maxY) {
        if (silkTouch) {
            energyPerTick = MathUtils.multiplyClamped(energyPerTick, MekanismConfig.general.minerSilkMultiplier.get());
        }
        //Ranges are difference between max and default
        double radiusRange = MekanismConfig.general.minerMaxRadius.get() - TileEntityDigitalMiner.DEFAULT_RADIUS;
        double heightRange;
        if (level == null) {
            //Default to a world height of 255 (pre 1.18 height)
            heightRange = 255 - TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE;
        } else {
            //Note: We adjust the height by one as the height range is "zero indexed"
            heightRange = level.getHeight() - 1 - TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE;
        }
        //If the range for a specific thing is zero, ignore it from the cost calculations
        double radiusCost;
        if (radiusRange == 0) {
            radiusCost = 0;
        } else {
            radiusCost = Math.max((radius - TileEntityDigitalMiner.DEFAULT_RADIUS) / radiusRange, 0);
        }
        double heightCost;
        if (heightRange == 0) {
            heightCost = 0;
        } else {
            heightCost = Math.max((maxY - minY - TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE) / heightRange, 0);
        }
        return Mth.ceil(energyPerTick * (1 + radiusCost) * (1 + heightCost));
    }
}