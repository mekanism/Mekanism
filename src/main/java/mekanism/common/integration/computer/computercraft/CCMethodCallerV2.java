package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class CCMethodCallerV2 extends BoundMethodHolder {
    public String[] getMethodNames() {
        return methodNames.get();
    }

    public MethodResult callMethod(ILuaContext context, int methodIdx, IArguments arguments) throws LuaException {
        String[] methodNames = this.methodNames.get();
        if (methodIdx > methodNames.length) {
            throw new LuaException(String.format(Locale.ROOT, "Method index '%d' is out of bounds. This only has '%d' methods.", methodIdx, methodNames.length));
        }
        Collection<MethodData> methodDataCollection = this.methods.get(methodNames[methodIdx]);
        int argCount = arguments.count();
        MethodData methodToCall = methodDataCollection.stream().filter(md -> md.arguments().length == argCount)
                .findAny()
                .orElseThrow(() -> new LuaException(String.format(Locale.ROOT,
                        "Found %d arguments, expected %s",
                        argCount,
                        methodDataCollection.stream().map(it -> String.valueOf(it.arguments().length)).collect(Collectors.joining(" or "))
                )));
        if (methodToCall.threadSafe()) {
            return callHandler(arguments, methodToCall);
        } else {
            return context.executeMainThreadTask(()->callHandler(arguments, methodToCall).getResult());
        }
    }

    @NotNull
    private static MethodResult callHandler(IArguments arguments, MethodData methodToCall) throws LuaException {
        Object result;
        try {
            result = methodToCall.handler().apply(methodToCall.subject(), new CCComputerHelper(arguments));
        } catch (ComputerException ex) {
            if (ex.getCause() instanceof LuaException luaException) {
                throw luaException;
            }
            throw (LuaException)new LuaException(ex.getMessage()).initCause(ex);
        }
        return result instanceof MethodResult mr ? mr : MethodResult.of(result);
    }
}
