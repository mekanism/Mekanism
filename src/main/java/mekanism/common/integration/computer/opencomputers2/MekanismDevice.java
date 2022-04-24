package mekanism.common.integration.computer.opencomputers2;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import li.cil.oc2.api.bus.device.rpc.RPCDevice;
import li.cil.oc2.api.bus.device.rpc.RPCMethodGroup;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MekanismDevice<TILE extends BlockEntity & IComputerTile> implements RPCDevice {

    /**
     * Only call this if the given tile actually has computer support as it won't be double-checked.
     */
    public static <TILE extends BlockEntity & IComputerTile> MekanismDevice<TILE> create(TILE tile) {
        //Linked map to ensure that the order is persisted
        Map<String, BoundComputerMethod> boundMethods = new LinkedHashMap<>();
        tile.getComputerMethods(boundMethods);
        return new MekanismDevice<>(tile, boundMethods);
    }

    private final List<RPCMethodGroup> methodGroups;
    private final List<String> name;

    private MekanismDevice(TILE tile, Map<String, BoundComputerMethod> boundMethods) {
        this.name = Collections.singletonList(tile.getComputerName());
        this.methodGroups = boundMethods.entrySet().stream().<RPCMethodGroup>map(entry -> new MekanismRPCMethodGroup(entry.getKey(), entry.getValue())).toList();
    }

    @Nonnull
    @Override
    public List<String> getTypeNames() {
        return name;
    }

    @Nonnull
    @Override
    public List<RPCMethodGroup> getMethodGroups() {
        return methodGroups;
    }
}