package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import javax.annotation.Nullable;
import mekanism.common.integration.computer.IComputerArgumentHandler;

//TODO: See if this properly handles/we need to handle CC's VarargArguments class as it has some overrides for things like getDouble and getLong
// I think that may just be internal implementation detail though and it doesn't really matter to us
public class CCArgumentWrapper implements IComputerArgumentHandler<LuaException, MethodResult> {

    private final IArguments arguments;

    public CCArgumentWrapper(IArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public int getCount() {
        return arguments.count();
    }

    @Override
    public LuaException error(String messageFormat, Object... args) {
        return new LuaException(String.format(messageFormat, args));
    }

    @Nullable
    @Override
    public Object getArgument(int index) {
        return arguments.get(index);
    }

    @Override
    public Object[] getArguments() {
        return arguments.getAll();
    }

    @Override
    public MethodResult noResult() {
        return MethodResult.of();
    }

    @Override
    public MethodResult wrapResult(Object result) {
        if (result instanceof Object[]) {
            //TODO: Validate this works properly especially for other types of arrays
            return MethodResult.of((Object[]) result);
        }
        return MethodResult.of(result);
    }
}