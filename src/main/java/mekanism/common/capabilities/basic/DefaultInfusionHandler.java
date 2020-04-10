package mekanism.common.capabilities.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DefaultInfusionHandler implements IInfusionHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IInfusionHandler.class, new NullStorage<>(), DefaultInfusionHandler::new);
    }

    @Override
    public int getInfusionTankCount() {
        return 1;
    }

    @Override
    public InfusionStack getInfusionInTank(int tank) {
        return InfusionStack.EMPTY;
    }

    @Override
    public void setInfusionInTank(int tank, InfusionStack stack) {
    }

    @Override
    public long getInfusionTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isInfusionValid(int tank, InfusionStack stack) {
        return true;
    }

    @Override
    public InfusionStack insertInfusion(int tank, InfusionStack stack, Action action) {
        return stack;
    }

    @Override
    public InfusionStack extractInfusion(int tank, long amount, Action action) {
        return InfusionStack.EMPTY;
    }
}