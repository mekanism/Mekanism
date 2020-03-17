package mekanism.common.capabilities.energy;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.block.IBlockElectric;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.block.Block;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinerEnergyContainer extends MachineEnergyContainer<TileEntityDigitalMiner> {

    public static MinerEnergyContainer input(TileEntityDigitalMiner tile) {
        Block block = tile.getBlockType().getBlock();
        if (!(block instanceof IBlockElectric)) {
            throw new IllegalArgumentException("Block provider must be an electric block");
        }
        Objects.requireNonNull(tile, "Tile cannot be null");
        IBlockElectric electricBlock = (IBlockElectric) block;
        return new MinerEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), tile);
    }

    private double minerEnergyPerTick;

    private MinerEnergyContainer(double maxEnergy, double energyPerTick, TileEntityDigitalMiner tile) {
        super(maxEnergy, energyPerTick, notExternal, alwaysTrue, tile);
    }

    @Override
    public double getEnergyPerTick() {
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
            minerEnergyPerTick *= MekanismConfig.general.minerSilkMultiplier.get();
        }
        minerEnergyPerTick *= 1 + Math.max((tile.getRadius() - 10) / 22D, 0);
        minerEnergyPerTick *= 1 + Math.max((tile.getMaxY() - tile.getMinY() - 60) / 195D, 0);
    }
}