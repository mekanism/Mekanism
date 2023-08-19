package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class MekanismPeripheral<TILE extends BlockEntity & IComputerTile> extends CCMethodCaller implements IDynamicPeripheral {
    /**
     * Only call this if the given tile actually has computer support as it won't be double-checked.
     */
    public static <TILE extends BlockEntity & IComputerTile> MekanismPeripheral<TILE> create(TILE tile) {
        MekanismPeripheral<TILE> mekanismPeripheral = new MekanismPeripheral<>(tile);
        tile.getComputerMethods(mekanismPeripheral);
        return mekanismPeripheral;
    }

    private final String name;
    private final WeakReference<TILE> tile;

    private MekanismPeripheral(TILE tile) {
        this.tile = new WeakReference<>(tile);
        this.name = tile.getComputerName();
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
        return tile.get();
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof MekanismPeripheral<?> && getTarget() == other.getTarget();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MekanismPeripheral<?> other && equals(other);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        TILE tileRef = tile.get();
        result = 31 * result + (tileRef != null ? tileRef.hashCode() : 0);
        return result;
    }
}
