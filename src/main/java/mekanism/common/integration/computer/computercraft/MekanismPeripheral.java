package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.integration.computer.ComputerMethod;
import mekanism.common.integration.computer.ComputerMethodHandler;

//TODO: Do we need to override getTarget, and if so what is it used for
//TODO: Look through https://github.com/SquidDev-CC/CC-Tweaked/commit/d5f82fa458fd5ed50292629e554f38650df1d588 in a lot more detail
public class MekanismPeripheral implements IDynamicPeripheral {

    @Nullable
    public static MekanismPeripheral getPeripheral(ComputerMethodHandler handler) {
        List<ComputerMethod> methods = handler.getMethods();
        if (methods.isEmpty()) {
            //TODO: Decide if we want this to be an exception instead?
            return null;
        }
        //TODO: Should we also pass the method handler in general, such as the tile's name?
        return new MekanismPeripheral(methods);
    }

    private final String[] methodNames;
    private final ComputerMethod[] methods;

    private MekanismPeripheral(List<ComputerMethod> methods) {
        this.methods = methods.toArray(new ComputerMethod[0]);
        this.methodNames = new String[this.methods.length];
        for (int i = 0; i < this.methods.length; i++) {
            this.methodNames[i] = this.methods[i].getMethodName();
        }
    }

    @Nonnull
    @Override
    public String getType() {
        //TODO: Implement, name of this peripheral/type. I believe an example would be: digitalMiner or energyCube
        return "";
    }

    @Override
    public boolean equals(@Nullable IPeripheral peripheral) {
        if (peripheral instanceof MekanismPeripheral) {
            //TODO: Implement
            //TODO: Do we want to keep track of the Coord4D our peripheral is at/for and
            // then only have them be equal if it is an instance of ours and the same coord
            return false;
        }
        return false;
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return methodNames;
    }

    @Nonnull
    @Override
    public MethodResult callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int methodIndex, @Nonnull IArguments arguments) throws LuaException {
        if (methodIndex < 0 || methodIndex >= methods.length) {
            //TODO: Improve this to better state the bounds
            throw error("Method index '%d' out of bounds.", methodIndex);
        }
        ComputerMethod method = methods[methodIndex];
        CCArgumentWrapper argumentWrapper = new CCArgumentWrapper(arguments);
        method.validateArguments(argumentWrapper);
        //TODO: Figure out about the ILuaContext and if we need to do stuff to make sure we are running on the correct thread
        return method.run(argumentWrapper);
    }

    private static LuaException error(String messageFormat, Object... args) {
        return new LuaException(String.format(messageFormat, args));
    }
}