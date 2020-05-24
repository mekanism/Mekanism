package mekanism.common.capabilities.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DefaultSlurryHandler implements ISlurryHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(ISlurryHandler.class, new NullStorage<>(), DefaultSlurryHandler::new);
    }

    @Override
    public int getSlurryTankCount() {
        return 1;
    }

    @Override
    public SlurryStack getSlurryInTank(int tank) {
        return SlurryStack.EMPTY;
    }

    @Override
    public void setSlurryInTank(int tank, SlurryStack stack) {
    }

    @Override
    public long getSlurryTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isSlurryValid(int tank, SlurryStack stack) {
        return true;
    }

    @Override
    public SlurryStack insertSlurry(int tank, SlurryStack stack, Action action) {
        return stack;
    }

    @Override
    public SlurryStack extractSlurry(int tank, long amount, Action action) {
        return SlurryStack.EMPTY;
    }
}