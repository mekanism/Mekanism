package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.functions.LongObjectToLongFunction;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ElectroSeparatorEnergyContainer extends MachineEnergyContainer<TileEntityElectrolyticSeparator> {

    public static ElectroSeparatorEnergyContainer input(TileEntityElectrolyticSeparator tile, LongObjectToLongFunction<TileEntityElectrolyticSeparator> baseEnergyCalculator,
          @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new ElectroSeparatorEnergyContainer(electricBlock.getUsage() * 4, electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, baseEnergyCalculator, listener);
    }

    private final LongObjectToLongFunction<TileEntityElectrolyticSeparator> baseEnergyCalculator;

    protected ElectroSeparatorEnergyContainer(long maxEnergy, long energyPerTick, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, TileEntityElectrolyticSeparator tile, LongObjectToLongFunction<TileEntityElectrolyticSeparator> baseEnergyCalculator, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
        this.baseEnergyCalculator = baseEnergyCalculator;
    }

    @Override
    public long getBaseEnergyPerTick() {
        return baseEnergyCalculator.applyAsLong(super.getBaseEnergyPerTick(), tile);
    }

    @Override
    public void updateEnergyPerTick() {
        if (tile.isMakingHydrogen()) {
            //Energy upgrades do nothing
            this.currentEnergyPerTick = getBaseEnergyPerTick();
        } else {
            super.updateEnergyPerTick();
        }
    }

    @Override
    public void updateMaxEnergy() {
        super.updateMaxEnergy();
        if (tile.isMakingHydrogen()) {
            setMaxEnergy(this.capacity() * tile.getBaselineMaxOperations() * 4);
        }
    }
}
