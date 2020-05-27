package mekanism.common.capabilities.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DefaultChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IChemicalHandler<CHEMICAL, STACK> {

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public STACK getChemicalInTank(int tank) {
        return getEmptyStack();
    }

    @Override
    public void setChemicalInTank(int tank, STACK stack) {
    }

    @Override
    public long getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isValid(int tank, STACK stack) {
        return true;
    }

    @Override
    public STACK insertChemical(int tank, STACK stack, Action action) {
        return stack;
    }

    @Override
    public STACK extractChemical(int tank, long amount, Action action) {
        return getEmptyStack();
    }

    public static class DefaultGasHandler extends DefaultChemicalHandler<Gas, GasStack> implements IGasHandler {

        public static void register() {
            CapabilityManager.INSTANCE.register(IGasHandler.class, new NullStorage<>(), DefaultGasHandler::new);
        }
    }

    public static class DefaultInfusionHandler extends DefaultChemicalHandler<InfuseType, InfusionStack> implements IInfusionHandler {

        public static void register() {
            CapabilityManager.INSTANCE.register(IInfusionHandler.class, new NullStorage<>(), DefaultInfusionHandler::new);
        }
    }

    public static class DefaultPigmentHandler extends DefaultChemicalHandler<Pigment, PigmentStack> implements IPigmentHandler {

        public static void register() {
            CapabilityManager.INSTANCE.register(IPigmentHandler.class, new NullStorage<>(), DefaultPigmentHandler::new);
        }
    }

    public static class DefaultSlurryHandler extends DefaultChemicalHandler<Slurry, SlurryStack> implements ISlurryHandler {

        public static void register() {
            CapabilityManager.INSTANCE.register(ISlurryHandler.class, new NullStorage<>(), DefaultSlurryHandler::new);
        }
    }
}