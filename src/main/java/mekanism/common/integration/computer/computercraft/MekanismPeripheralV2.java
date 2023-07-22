package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class MekanismPeripheralV2<TILE extends BlockEntity & IComputerTile> extends CCMethodCallerV2 implements IDynamicPeripheral {
    /**
     * Only call this if the given tile actually has computer support as it won't be double-checked.
     */
    public static <TILE extends BlockEntity & IComputerTile> MekanismPeripheralV2<TILE> create(TILE tile) {
        //Linked map to ensure that the order is persisted
        Map<String, BoundComputerMethod> boundMethods = new LinkedHashMap<>();
        tile.getComputerMethods(boundMethods);
        return new MekanismPeripheralV2<>(tile);
    }

    private final String name;
    private final TILE tile;

    private MekanismPeripheralV2(TILE tile) {
        this.tile = tile;
        this.name = this.tile.getComputerName();
    }

    @Override
    public MethodResult callMethod(IComputerAccess computer, ILuaContext context, int method, IArguments arguments) throws LuaException {
        return callMethod(context, method, arguments);
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
}
