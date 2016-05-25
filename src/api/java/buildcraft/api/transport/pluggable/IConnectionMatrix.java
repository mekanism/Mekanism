package buildcraft.api.transport.pluggable;

import net.minecraft.util.EnumFacing;

public interface IConnectionMatrix {
    boolean isConnected(EnumFacing face);
}
