package mekanism.common.capabilities.energy;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.world.level.Level;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinerEnergyContainer extends MachineEnergyContainer<TileEntityDigitalMiner> {

    public static MinerEnergyContainer input(TileEntityDigitalMiner tile) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new MinerEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), tile);
    }

    private FloatingLong minerEnergyPerTick;

    private MinerEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick, TileEntityDigitalMiner tile) {
        super(maxEnergy, energyPerTick, notExternal, alwaysTrue, tile);
        this.minerEnergyPerTick = getBaseEnergyPerTick();
    }

    @Override
    public void setEnergyPerTick(FloatingLong energyPerTick) {
        super.setEnergyPerTick(energyPerTick);
        this.minerEnergyPerTick = energyPerTick;
    }

    @Override
    public FloatingLong getEnergyPerTick() {
        return minerEnergyPerTick;
    }

    @Override
    public void updateEnergyPerTick() {
        super.updateEnergyPerTick();
        //Also update our miner's energy specific values
        updateMinerEnergyPerTick();
    }

    public void updateMinerEnergyPerTick() {
        minerEnergyPerTick = super.getEnergyPerTick();
        if (tile.getSilkTouch()) {
            minerEnergyPerTick = minerEnergyPerTick.multiply(MekanismConfig.general.minerSilkMultiplier.get());
        }
        double radiusRange = MekanismConfig.general.minerMaxRadius.get() - TileEntityDigitalMiner.DEFAULT_RADIUS;
        double heightRange;
        Level level = tile.getLevel();
        if (level == null) {
            //Default to a world height of 255 (pre 1.18 height)
            heightRange = 255 - TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE;
        } else {
            //Note: We adjust the height by one as the height range is "zero indexed"
            heightRange = level.getHeight() - 1 - TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE;
        }
        //If the range for a specific thing is zero, ignore it from the cost calculations
        double radiusCost = radiusRange == 0 ? 0 : (tile.getRadius() - TileEntityDigitalMiner.DEFAULT_RADIUS) / radiusRange;
        double heightCost = heightRange == 0 ? 0 : (tile.getMaxY() - tile.getMinY() - TileEntityDigitalMiner.DEFAULT_HEIGHT_RANGE) / heightRange;
        minerEnergyPerTick = minerEnergyPerTick.multiply((1 + Math.max(radiusCost, 0)) * (1 + Math.max(heightCost, 0)));
    }
}