package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.capabilities.resolver.manager.chemical.ChemicalHandlerManager;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.NonNullSupplier;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DynamicChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    private final NonNullSupplier<ChemicalHandlerManager<CHEMICAL, STACK, TANK, ?, ?>> handlerManager;
    private final InteractPredicate canExtract;
    private final InteractPredicate canInsert;
    private final Runnable onContentsChanged;

    //TODO: Note that the supplier should basically just return a constant.
    protected DynamicChemicalHandler(NonNullSupplier<ChemicalHandlerManager<CHEMICAL, STACK, TANK, ?, ?>> handlerManager, InteractPredicate canExtract,
          InteractPredicate canInsert, Runnable onContentsChanged) {
        this.handlerManager = handlerManager;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.onContentsChanged = onContentsChanged;
    }

    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return handlerManager.get().getContainers(side);
    }

    @Override
    public void onContentsChanged() {
        onContentsChanged.run();
    }

    @Override
    public STACK insertChemical(int tank, STACK stack, @Nullable Direction side, Action action) {
        //If we can insert into the specific tank from that side, try to. Otherwise exit
        return canInsert.test(tank, side) ? IMekanismChemicalHandler.super.insertChemical(tank, stack, side, action) : stack;
    }

    @Override
    public STACK extractChemical(int tank, long amount, @Nullable Direction side, Action action) {
        //If we can extract from a specific tank from a given side, try to. Otherwise exit
        return canExtract.test(tank, side) ? IMekanismChemicalHandler.super.extractChemical(tank, amount, side, action) : getEmptyStack();
    }

    @FunctionalInterface
    public interface InteractPredicate {

        boolean test(int tank, @Nullable Direction side);
    }
}