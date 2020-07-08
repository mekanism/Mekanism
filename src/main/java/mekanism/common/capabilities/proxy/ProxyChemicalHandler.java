package mekanism.common.capabilities.proxy;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.ISidedGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionHandler.ISidedInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentHandler.ISidedPigmentHandler;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler.ISidedSlurryHandler;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ProxyChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      SIDED_HANDLER extends ISidedChemicalHandler<CHEMICAL, STACK>> extends ProxyHandler implements IChemicalHandler<CHEMICAL, STACK> {

    private final SIDED_HANDLER sidedHandler;

    public ProxyChemicalHandler(SIDED_HANDLER sidedHandler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.sidedHandler = sidedHandler;
    }

    /**
     * @apiNote This is only for use in the TOP integration to allow us to properly handle hiding merged chemical tanks, and <strong>SHOULD NOT</strong> be called from
     * anywhere else. It is also important to not use this to bypass write access the proxy may limit.
     */
    public <TANK extends IChemicalTank<CHEMICAL, STACK>> List<TANK> getTanksIfMekanism() {
        if (sidedHandler instanceof IMekanismChemicalHandler) {
            return ((IMekanismChemicalHandler<CHEMICAL, STACK, TANK>) sidedHandler).getChemicalTanks(null);
        }
        return Collections.emptyList();
    }

    @Override
    public int getTanks() {
        return sidedHandler.getTanks(side);
    }

    @Override
    public STACK getChemicalInTank(int tank) {
        return sidedHandler.getChemicalInTank(tank, side);
    }

    @Override
    public void setChemicalInTank(int tank, STACK stack) {
        if (!readOnly) {
            sidedHandler.setChemicalInTank(tank, stack, side);
        }
    }

    @Override
    public long getTankCapacity(int tank) {
        return sidedHandler.getTankCapacity(tank, side);
    }

    @Override
    public boolean isValid(int tank, STACK stack) {
        return !readOnly || sidedHandler.isValid(tank, stack, side);
    }

    @Override
    public STACK insertChemical(int tank, STACK stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : sidedHandler.insertChemical(tank, stack, side, action);
    }

    @Override
    public STACK extractChemical(int tank, long amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? getEmptyStack() : sidedHandler.extractChemical(tank, amount, side, action);
    }

    @Override
    public STACK insertChemical(STACK stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : sidedHandler.insertChemical(stack, side, action);
    }

    @Override
    public STACK extractChemical(long amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? getEmptyStack() : sidedHandler.extractChemical(amount, side, action);
    }

    @Override
    public STACK extractChemical(STACK stack, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? getEmptyStack() : sidedHandler.extractChemical(stack, side, action);
    }

    public static class ProxyGasHandler extends ProxyChemicalHandler<Gas, GasStack, ISidedGasHandler> implements IGasHandler {

        public ProxyGasHandler(@Nonnull ISidedGasHandler gasHandler, @Nullable Direction side, @Nullable IHolder holder) {
            super(gasHandler, side, holder);
        }
    }

    public static class ProxyInfusionHandler extends ProxyChemicalHandler<InfuseType, InfusionStack, ISidedInfusionHandler> implements IInfusionHandler {

        public ProxyInfusionHandler(@Nonnull ISidedInfusionHandler infusionHandler, @Nullable Direction side, @Nullable IHolder holder) {
            super(infusionHandler, side, holder);
        }
    }

    public static class ProxyPigmentHandler extends ProxyChemicalHandler<Pigment, PigmentStack, ISidedPigmentHandler> implements IPigmentHandler {

        public ProxyPigmentHandler(@Nonnull ISidedPigmentHandler pigmentHandler, @Nullable Direction side, @Nullable IHolder holder) {
            super(pigmentHandler, side, holder);
        }
    }

    public static class ProxySlurryHandler extends ProxyChemicalHandler<Slurry, SlurryStack, ISidedSlurryHandler> implements ISlurryHandler {

        public ProxySlurryHandler(@Nonnull ISidedSlurryHandler slurryHandler, @Nullable Direction side, @Nullable IHolder holder) {
            super(slurryHandler, side, holder);
        }
    }
}