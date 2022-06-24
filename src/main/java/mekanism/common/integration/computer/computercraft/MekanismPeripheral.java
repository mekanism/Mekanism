package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MekanismPeripheral<TILE extends BlockEntity & IComputerTile> extends CCMethodCaller implements IDynamicPeripheral {

    /**
     * Only call this if the given tile actually has computer support as it won't be double-checked.
     */
    public static <TILE extends BlockEntity & IComputerTile> MekanismPeripheral<TILE> create(TILE tile) {
        //Linked map to ensure that the order is persisted
        Map<String, BoundComputerMethod> boundMethods = new LinkedHashMap<>();
        tile.getComputerMethods(boundMethods);
        return new MekanismPeripheral<>(tile, boundMethods);
    }

    private final String name;
    private final TILE tile;

    private MekanismPeripheral(TILE tile, Map<String, BoundComputerMethod> boundMethods) {
        super(boundMethods);
        this.tile = tile;
        this.name = this.tile.getComputerName();
    }

    @Override
    protected String getCallerType() {
        return "peripheral";
    }

    @Override
    public String getType() {
        return name;
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

    @Override
    public MethodResult callMethod(IComputerAccess computer, ILuaContext context, int methodIndex, IArguments arguments) throws LuaException {
        return callMethod(context, methodIndex, arguments);
    }
}