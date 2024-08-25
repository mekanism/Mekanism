package mekanism.common.capabilities.proxy;

import java.util.Collections;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyChemicalHandler extends ProxyHandler implements IChemicalHandler {

    private final ISidedChemicalHandler sidedHandler;

    public ProxyChemicalHandler(ISidedChemicalHandler sidedHandler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.sidedHandler = sidedHandler;
    }

    public ISidedChemicalHandler getInternalHandler() {
        return sidedHandler;
    }

    /**
     * @apiNote This is only for use in the TOP integration to allow us to properly handle hiding merged chemical tanks, and <strong>SHOULD NOT</strong> be called from
     * anywhere else. It is also important to not use this to bypass write access the proxy may limit.
     */
    public List<IChemicalTank> getTanksIfMekanism() {
        if (sidedHandler instanceof IMekanismChemicalHandler mekHandler) {
            return mekHandler.getChemicalTanks(null);
        }
        return Collections.emptyList();
    }

    @Override
    public int getChemicalTanks() {
        return sidedHandler.getCountChemicalTanks(side);
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return sidedHandler.getChemicalInTank(tank, side);
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        if (!readOnly) {
            sidedHandler.setChemicalInTank(tank, stack, side);
        }
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return sidedHandler.getChemicalTankCapacity(tank, side);
    }

    @Override
    public boolean isValid(int tank, ChemicalStack stack) {
        return !readOnly || sidedHandler.isValid(tank, stack, side);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        return readOnlyInsert() ? stack : sidedHandler.insertChemical(tank, stack, side, action);
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        return readOnlyExtract() ? ChemicalStack.EMPTY : sidedHandler.extractChemical(tank, amount, side, action);
    }

    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, Action action) {
        return readOnlyInsert() ? stack : sidedHandler.insertChemical(stack, side, action);
    }

    @Override
    public ChemicalStack extractChemical(long amount, Action action) {
        return readOnlyExtract() ? ChemicalStack.EMPTY : sidedHandler.extractChemical(amount, side, action);
    }

    @Override
    public ChemicalStack extractChemical(ChemicalStack stack, Action action) {
        return readOnlyExtract() ? ChemicalStack.EMPTY : sidedHandler.extractChemical(stack, side, action);
    }

}