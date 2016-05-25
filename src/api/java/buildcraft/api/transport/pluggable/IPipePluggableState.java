package buildcraft.api.transport.pluggable;

import net.minecraft.util.EnumFacing;

public interface IPipePluggableState {
    IConnectionMatrix getPluggableConnections();

    PipePluggable getPluggable(EnumFacing face);
}
