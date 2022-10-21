package mekanism.common.capabilities.holder.chemical;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProxiedChemicalTankHolder<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      extends ProxiedHolder implements IChemicalTankHolder<CHEMICAL, STACK, TANK> {

    private final Function<Direction, List<TANK>> tankFunction;

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
    ProxiedChemicalTankHolder<CHEMICAL, STACK, TANK> create(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<TANK>> tankFunction) {
        return new ProxiedChemicalTankHolder<>(insertPredicate, extractPredicate, tankFunction);
    }

    private ProxiedChemicalTankHolder(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate, Function<Direction, List<TANK>> tankFunction) {
        super(insertPredicate, extractPredicate);
        this.tankFunction = tankFunction;
    }

    @NotNull
    @Override
    public List<TANK> getTanks(@Nullable Direction side) {
        return tankFunction.apply(side);
    }
}