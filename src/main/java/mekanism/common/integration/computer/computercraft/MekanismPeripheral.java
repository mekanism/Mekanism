package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaCallback;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.BoundComputerMethod.SelectedMethodInfo;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.tileentity.TileEntity;

//TODO - 10.1: Try to find a way to expose a utility class to allow for converting between Joules and things like FE
// based on the set config values so that people can use that and have their script still work properly if the configs
// get changed to adjust the rates. It is possible to define things in a data pack https://github.com/SquidDev-CC/datapack-example
// to do this, but I am not sure we would be able to have them be modifiable via the configs at which point it may not be worthwhile
public class MekanismPeripheral<TILE extends TileEntity & IComputerTile> implements IDynamicPeripheral {

    private final BoundComputerMethod[] methods;
    private final String[] methodNames;
    private final TILE tile;

    /**
     * Only call this if the given tile actually has computer support as it won't be double checked.
     */
    public MekanismPeripheral(TILE tile) {
        this.tile = tile;
        //Linked map to ensure that the order is persisted
        Map<String, BoundComputerMethod> boundMethods = new LinkedHashMap<>();
        tile.getComputerMethods(boundMethods);
        this.methods = new BoundComputerMethod[boundMethods.size()];
        this.methodNames = new String[this.methods.length];
        int i = 0;
        for (Map.Entry<String, BoundComputerMethod> entry : boundMethods.entrySet()) {
            this.methodNames[i] = entry.getKey();
            this.methods[i] = entry.getValue();
            i++;
        }
    }

    @Nonnull
    @Override
    public String getType() {
        return tile.getType().getRegistryName().toString();
    }

    @Override
    public Object getTarget() {
        return tile;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        //Note: Check if we are the same object as the other one, otherwise consider us to not be equal as we
        // only will really be creating a single instance of this, and other instances of the same tile may
        // be invalid if it is not persistent such as for multiblocks
        return other == this;
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
            throw new LuaException(String.format(Locale.ROOT, "Method index '%d' is out of bounds. This peripheral only has '%d' methods.", methodIndex,
                  methods.length));
        }
        BoundComputerMethod method = methods[methodIndex];
        CCArgumentWrapper argumentWrapper = new CCArgumentWrapper(arguments);
        //Our argument type validator should be thread-safe, so can be ran on the ComputerCraft Lua thread
        SelectedMethodInfo selectedImplementation = method.findMatchingImplementation(argumentWrapper);
        if (selectedImplementation.isThreadSafe()) {
            //If our selected implementation is thread-safe, run it directly
            return method.run(argumentWrapper, selectedImplementation);
        }
        //Otherwise if it is not thread-safe (which will be the majority of our cases), queue it up to run on the game thread
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