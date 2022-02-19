package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaCallback;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.BoundComputerMethod.SelectedMethodInfo;

public abstract class CCMethodCaller {

    private final BoundComputerMethod[] methods;
    private final String[] methodNames;

    protected CCMethodCaller(Map<String, BoundComputerMethod> boundMethods) {
        this.methods = new BoundComputerMethod[boundMethods.size()];
        this.methodNames = new String[this.methods.length];
        int i = 0;
        for (Map.Entry<String, BoundComputerMethod> entry : boundMethods.entrySet()) {
            this.methodNames[i] = entry.getKey();
            this.methods[i] = entry.getValue();
            i++;
        }
    }

    protected abstract String getCallerType();

    @Nonnull
    public String[] getMethodNames() {
        return methodNames;
    }

    //Note: This method intentionally matches the signature for IDynamicLuaObject, but this class doesn't implement it to make sure
    // the peripheral doesn't have issues if something is doing an instance check. (There may not be any cases this is a problem)
    @Nonnull
    public MethodResult callMethod(@Nonnull ILuaContext context, int methodIndex, @Nonnull IArguments arguments) throws LuaException {
        if (methodIndex < 0 || methodIndex >= methods.length) {
            throw new LuaException(String.format(Locale.ROOT, "Method index '%d' is out of bounds. This %s only has '%d' methods.", methodIndex, getCallerType(),
                  methods.length));
        }
        BoundComputerMethod method = methods[methodIndex];
        CCArgumentWrapper argumentWrapper = new CCArgumentWrapper(arguments);
        //Our argument type validator should be thread-safe, so can be run on the ComputerCraft Lua thread
        SelectedMethodInfo selectedImplementation = method.findMatchingImplementation(argumentWrapper);
        if (selectedImplementation.isThreadSafe()) {
            //If our selected implementation is thread-safe, run it directly
            return method.run(argumentWrapper, selectedImplementation);
        }
        //Otherwise, if it is not thread-safe (which will be the majority of our cases), queue it up to run on the game thread
        long task = context.issueMainThreadTask(() -> method.run(argumentWrapper, selectedImplementation).getResult());
        return new TaskCallback(task).pull;
    }

    /**
     * Basically a copy of dan200.computercraft.core.asm.TaskCallback as suggested on https://github.com/SquidDev-CC/CC-Tweaked/discussions/728 due to there not being a
     * method via the API to do this. Ideally eventually it will be replaced by a method on ILuaContext that we can just call
     *
     * https://github.com/SquidDev-CC/CC-Tweaked/blob/mc-1.16.x/LICENSE
     */
    private static class TaskCallback implements ILuaCallback {

        private final MethodResult pull = MethodResult.pullEvent("task_complete", this);
        private final long task;

        private TaskCallback(long task) {
            this.task = task;
        }

        @Nonnull
        @Override
        public MethodResult resume(Object[] response) throws LuaException {
            if (response.length >= 3 && response[1] instanceof Number && response[2] instanceof Boolean) {
                if (((Number) response[1]).longValue() != this.task) {
                    return this.pull;
                } else if ((Boolean) response[2]) {
                    return MethodResult.of(Arrays.copyOfRange(response, 3, response.length));
                } else if (response.length >= 4 && response[3] instanceof String) {
                    throw new LuaException((String) response[3]);
                }
                throw new LuaException("error");
            }
            return this.pull;
        }
    }
}