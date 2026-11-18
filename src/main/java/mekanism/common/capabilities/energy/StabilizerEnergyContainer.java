package mekanism.common.capabilities.energy;

import mekanism.api.IContentsListener;
import mekanism.api.functions.IntObjectToIntFunction;
import mekanism.api.math.MathUtils;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.Holder.Reference;
import org.jspecify.annotations.Nullable;

public class StabilizerEnergyContainer extends MachineEnergyContainer<TileEntityDimensionalStabilizer> {

    private static final IntObjectToIntFunction<TileEntityDimensionalStabilizer> BASE_ENERGY_CALCULATOR = (base, tile) -> MathUtils.multiplyClamped(base, tile.getChunksLoaded());

    private final int maxEnergyPerTick;

    public StabilizerEnergyContainer(TileEntityDimensionalStabilizer tile, @Nullable IContentsListener listener) {
        super(tile, BASE_ENERGY_CALCULATOR, listener);
        maxEnergyPerTick = attributeEnergy.getUsage() * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER * TileEntityDimensionalStabilizer.MAX_LOAD_DIAMETER;
    }

    @Override
    protected long calculateMaxEnergy(int energyPerTick, long bufferMultiplier, @Nullable Reference<Upgrade> speedUpgrade, @Nullable Reference<Upgrade> energyUpgrade) {
        if (energyUpgrade == null) {
            return super.calculateMaxEnergy(energyPerTick, bufferMultiplier, null, null);
        }
        //Override handling to special case the stabilizer so that the energy buffer can only reach its maximum potential with full energy upgrades
        double fractionEnergy = UpgradeUtils.fractionUpgrades(tile, energyUpgrade);
        return MathUtils.clampToLong(attributeEnergy.getBaseMultiplier() * maxEnergyPerTick * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionEnergy - 1));
    }

    @Override
    protected int calculateEnergyPerTick(int baseEnergyPerTick, @Nullable Reference<Upgrade> speedUpgrade, @Nullable Reference<Upgrade> energyUpgrade) {
        //Theoretically super will end up just returning this, but short circuit it and ensure it does just in case a datapack changes the supported upgrades
        return baseEnergyPerTick;
    }
}
