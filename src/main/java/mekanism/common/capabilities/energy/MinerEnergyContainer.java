package mekanism.common.capabilities.energy;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.machine.TileEntityDigitalMiner;

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
        minerEnergyPerTick = minerEnergyPerTick.multiply((1 + Math.max((tile.getRadius() - 10) / 22D, 0)) *
                                                         (1 + Math.max((tile.getMaxY() - tile.getMinY() - 60) / 195D, 0)));
    }
}