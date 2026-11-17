package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.functions.IntObjectToIntFunction;
import mekanism.api.math.MathUtils;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter;
import org.jspecify.annotations.Nullable;

public class StabilizerEnergyContainer extends MachineEnergyContainer<TileEntityDimensionalStabilizer> {

    public static StabilizerEnergyContainer input(TileEntityDimensionalStabilizer tile, IntObjectToIntFunction<TileEntityDimensionalStabilizer> baseEnergyCalculator,
          @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new StabilizerEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, baseEnergyCalculator, listener);
    }

    private StabilizerEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
          TileEntityDimensionalStabilizer tile, IntObjectToIntFunction<TileEntityDimensionalStabilizer> baseEnergyCalculator, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, baseEnergyCalculator, listener);
    }

    @Override
    public void updateEnergy(HolderGetter<Upgrade> upgrades) {
        setEnergyPerTick(getBaseEnergyPerTick());
        //Override handling to special case the stabilizer so that the energy buffer actually increases with the number of energy upgrades
        Reference<Upgrade> energyUpgrade = upgrades.get(UpgradeIds.ENERGY).orElse(null);
        double fractionEnergy = energyUpgrade == null ? 0 : UpgradeUtils.fractionUpgrades(tile, energyUpgrade);
        setMaxEnergy(MathUtils.clampToLong(getBaseMaxEnergy() * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionEnergy)));
    }
}
