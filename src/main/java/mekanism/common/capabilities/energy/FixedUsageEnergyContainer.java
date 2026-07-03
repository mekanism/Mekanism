package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.functions.IntObjectToIntFunction;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.Nullable;

public class FixedUsageEnergyContainer<TILE extends TileEntityMekanism> extends MachineEnergyContainer<TILE> {

    public static <TILE extends TileEntityMekanism> FixedUsageEnergyContainer<TILE> input(TILE tile, IntObjectToIntFunction<TILE> baseEnergyCalculator,
          @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new FixedUsageEnergyContainer<>(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, baseEnergyCalculator, listener);
    }

    private final IntObjectToIntFunction<TILE> baseEnergyCalculator;

    protected FixedUsageEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract,
          Predicate<AutomationType> canInsert, TILE tile, IntObjectToIntFunction<TILE> baseEnergyCalculator, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
        this.baseEnergyCalculator = baseEnergyCalculator;
    }

    @Override
    public int getBaseEnergyPerTick() {
        return baseEnergyCalculator.applyAsInt(super.getBaseEnergyPerTick(), tile);
    }

    @Override
    public void updateEnergyPerTick(HolderLookup.@Nullable Provider registries) {
        //Energy upgrades only increase storage
        this.currentEnergyPerTick = getBaseEnergyPerTick();
    }
}
