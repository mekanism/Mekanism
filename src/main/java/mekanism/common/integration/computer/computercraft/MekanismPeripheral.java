package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.tileentity.TileEntity;

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
        //TODO - 10.1: Test this and if it makes sense or if we need to have our tile implement a class and return something like
        // digitalMiner or energyCube
        return tile.getType().getRegistryName().toString();
    }

    @Override
    public Object getTarget() {
        return tile;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        //Consider to peripherals equal if they are backed by the same tile
        return other == this || other != null && getClass() == other.getClass() && tile == ((MekanismPeripheral<?>) other).tile;
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
        method.validateArguments(argumentWrapper);
        //TODO - 10.1: Figure out about the ILuaContext and if we need to do stuff to make sure we are running on the correct thread
        return method.run(argumentWrapper);
    }
}