package mekanism.common.capabilities.holder.chemical;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//todo remove?
public class ProxiedChemicalTankHolder extends ProxiedHolder implements IChemicalTankHolder {

    private final Function<Direction, List<IChemicalTank>> tankFunction;

    public static ProxiedChemicalTankHolder create(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<IChemicalTank>> tankFunction) {
        return new ProxiedChemicalTankHolder(insertPredicate, extractPredicate, tankFunction);
    }

    private ProxiedChemicalTankHolder(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate, Function<Direction, List<IChemicalTank>> tankFunction) {
        super(insertPredicate, extractPredicate);
        this.tankFunction = tankFunction;
    }

    @NotNull
    @Override
    public List<IChemicalTank> getTanks(@Nullable Direction side) {
        return tankFunction.apply(side);
    }
}