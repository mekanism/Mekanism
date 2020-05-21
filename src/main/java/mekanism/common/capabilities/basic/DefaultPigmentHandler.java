package mekanism.common.capabilities.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DefaultPigmentHandler implements IPigmentHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IPigmentHandler.class, new NullStorage<>(), DefaultPigmentHandler::new);
    }

    @Override
    public int getPigmentTankCount() {
        return 1;
    }

    @Override
    public PigmentStack getPigmentInTank(int tank) {
        return PigmentStack.EMPTY;
    }

    @Override
    public void setPigmentInTank(int tank, PigmentStack stack) {
    }

    @Override
    public long getPigmentTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isPigmentValid(int tank, PigmentStack stack) {
        return true;
    }

    @Override
    public PigmentStack insertPigment(int tank, PigmentStack stack, Action action) {
        return stack;
    }

    @Override
    public PigmentStack extractPigment(int tank, long amount, Action action) {
        return PigmentStack.EMPTY;
    }
}