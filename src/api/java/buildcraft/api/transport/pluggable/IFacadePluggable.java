package buildcraft.api.transport.pluggable;

import net.minecraft.block.state.IBlockState;

public interface IFacadePluggable {
    IBlockState getCurrentState();

    boolean isTransparent();

    boolean isHollow();
}
