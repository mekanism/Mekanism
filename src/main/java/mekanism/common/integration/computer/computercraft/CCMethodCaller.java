package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.NotNull;

public class CCMethodCaller extends BoundMethodHolder {

    public String[] getMethodNames() {
        return methodNames.get();
    }

    public MethodResult callMethod(ILuaContext context, int methodIdx, IArguments arguments) throws LuaException {
        String[] methodNames = this.getMethodNames();
        if (methodIdx >= methodNames.length) {
            throw new LuaException(String.format(Locale.ROOT, "Method index '%d' is out of bounds. This handler only has '%d' methods.", methodIdx, methodNames.length));
        }
        //validate arg counts match, types are checked at call time
        Collection<BoundMethodData<?>> methodDataCollection = this.methods.get(methodNames[methodIdx]);
        int argCount = arguments.count();
        BoundMethodData<?> methodToCall = getBoundMethodData(methodDataCollection, argCount);
        if (methodToCall.threadSafe()) {
            return callHandler(arguments, methodToCall);
        }
        arguments.escapes();
        return context.executeMainThreadTask(() -> callHandler(arguments, methodToCall).getResult());
    }

    private static BoundMethodData<?> getBoundMethodData(Collection<BoundMethodData<?>> methodDataCollection, int argCount) throws LuaException {
        for (BoundMethodData<?> md : methodDataCollection) {
            if (md.argumentNames().length == argCount) {
                return md;
            }
        }
        throw new LuaException(String.format(Locale.ROOT, "Found %d arguments, expected %s", argCount,
              methodDataCollection.stream().map(it -> String.valueOf(it.argumentNames().length)).collect(Collectors.joining(" or "))
        ));
    }

    @NotNull
    private static MethodResult callHandler(IArguments arguments, BoundMethodData<?> methodToCall) throws LuaException {
        Object result;
        try {
            result = methodToCall.call(new CCComputerHelper(arguments));
        } catch (ComputerException ex) {
            if (ex.getCause() instanceof LuaException luaException) {
                throw luaException;
            }
            throw (LuaException) new LuaException(ex.getMessage()).initCause(ex);
        }
        return result instanceof MethodResult mr ? mr : MethodResult.of(result);
    }
}
