package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.Locale;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.BoundComputerMethod.SelectedMethodInfo;

@NothingNullByDefault
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

    public String[] getMethodNames() {
        return methodNames;
    }

    //Note: This method intentionally matches the signature for IDynamicLuaObject, but this class doesn't implement it to make sure
    // the peripheral doesn't have issues if something is doing an instance check. (There may not be any cases this is a problem)
    public MethodResult callMethod(ILuaContext context, int methodIndex, IArguments arguments) throws LuaException {
        if (methodIndex < 0 || methodIndex >= methods.length) {
            throw new LuaException(String.format(Locale.ROOT, "Method index '%d' is out of bounds. This %s only has '%d' methods.", methodIndex, getCallerType(),
                  methods.length));
        }
        BoundComputerMethod method = methods[methodIndex];
        //Note: Even though we would not have to escape for if the method is thread safe we need to do so as finding our matching implementation
        // may maintain references to the corresponding arguments and if those arguments require escaping then we don't have a simple way to do
        // so later
        CCArgumentWrapper argumentWrapper = new CCArgumentWrapper(arguments.escapes());
        //Our argument type validator should be thread-safe, so can be run on the ComputerCraft Lua thread
        SelectedMethodInfo selectedImplementation = method.findMatchingImplementation(argumentWrapper);
        if (selectedImplementation.getMethod().threadSafe()) {
            //If our selected implementation is thread-safe, run it directly
            return method.run(argumentWrapper, selectedImplementation);
        }
        //Otherwise, if it is not thread-safe (which will be the majority of our cases),queue the task up to run on the game thread
        return context.executeMainThreadTask(() -> method.run(argumentWrapper, selectedImplementation).getResult());
    }
}