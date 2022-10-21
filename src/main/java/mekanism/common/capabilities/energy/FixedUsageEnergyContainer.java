package mekanism.common.capabilities.energy;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.base.TileEntityMekanism;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FixedUsageEnergyContainer<TILE extends TileEntityMekanism> extends MachineEnergyContainer<TILE> {

    public static <TILE extends TileEntityMekanism> FixedUsageEnergyContainer<TILE> input(TILE tile, BiFunction<FloatingLong, TILE, FloatingLong> baseEnergyCalculator,
          @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new FixedUsageEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile, baseEnergyCalculator, listener);
    }

    private final BiFunction<FloatingLong, TILE, FloatingLong> baseEnergyCalculator;

    protected FixedUsageEnergyContainer(FloatingLong maxEnergy, FloatingLong energyPerTick, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, TILE tile, BiFunction<FloatingLong, TILE, FloatingLong> baseEnergyCalculator, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
        this.baseEnergyCalculator = baseEnergyCalculator;
    }

    @Override
    public FloatingLong getBaseEnergyPerTick() {
        return baseEnergyCalculator.apply(super.getBaseEnergyPerTick(), tile);
    }

    @Override
    public void updateEnergyPerTick() {
        //Energy upgrades only increase storage
        this.currentEnergyPerTick = getBaseEnergyPerTick();
    }
}
