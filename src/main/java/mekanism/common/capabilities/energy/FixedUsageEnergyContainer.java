package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.functions.LongObjectToLongFunction;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.base.TileEntityMekanism;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FixedUsageEnergyContainer<TILE extends TileEntityMekanism> extends MachineEnergyContainer<TILE> {

    public static <TILE extends TileEntityMekanism> FixedUsageEnergyContainer<TILE> input(TILE tile, LongObjectToLongFunction<TILE> baseEnergyCalculator,
          @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new FixedUsageEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, baseEnergyCalculator, listener);
    }

    private final LongObjectToLongFunction<TILE> baseEnergyCalculator;

    protected FixedUsageEnergyContainer(long maxEnergy, long energyPerTick, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, TILE tile, LongObjectToLongFunction<TILE> baseEnergyCalculator, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
        this.baseEnergyCalculator = baseEnergyCalculator;
    }

    @Override
    public long getBaseEnergyPerTick() {
        return baseEnergyCalculator.applyAsLong(super.getBaseEnergyPerTick(), tile);
    }

    @Override
    public void updateEnergyPerTick() {
        //Energy upgrades only increase storage
        this.currentEnergyPerTick = getBaseEnergyPerTick();
    }
}
