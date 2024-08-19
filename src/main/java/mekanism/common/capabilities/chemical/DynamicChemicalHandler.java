package mekanism.common.capabilities.chemical;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.capabilities.DynamicHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DynamicChemicalHandler extends DynamicHandler<IChemicalTank> implements IMekanismChemicalHandler {

    public DynamicChemicalHandler(Function<Direction, List<IChemicalTank>> tankSupplier, Predicate<@Nullable Direction> canExtract,
          Predicate<@Nullable Direction> canInsert, @Nullable IContentsListener listener) {
        super(tankSupplier, canExtract, canInsert, listener);
    }

    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return containerSupplier.apply(side);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, @Nullable Direction side, Action action) {
        //If we can insert into the specific side, try to. Otherwise exit
        return canInsert.test(side) ? IMekanismChemicalHandler.super.insertChemical(tank, stack, side, action) : stack;
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, @Nullable Direction side, Action action) {
        //If we can extract from a specific side, try to. Otherwise exit
        return canExtract.test(side) ? IMekanismChemicalHandler.super.extractChemical(tank, amount, side, action) : ChemicalStack.EMPTY;
    }

    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        //If we can insert into the specific side, try to. Otherwise exit
        return canInsert.test(side) ? IMekanismChemicalHandler.super.insertChemical(stack, side, action) : stack;
    }

    @Override
    public ChemicalStack extractChemical(long amount, @Nullable Direction side, Action action) {
        //If we can extract from a specific side, try to. Otherwise exit
        return canExtract.test(side) ? IMekanismChemicalHandler.super.extractChemical(amount, side, action) : ChemicalStack.EMPTY;
    }

    @Override
    public ChemicalStack extractChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        //If we can extract from a specific side, try to. Otherwise exit
        return canExtract.test(side) ? IMekanismChemicalHandler.super.extractChemical(stack, side, action) : ChemicalStack.EMPTY;
    }

}