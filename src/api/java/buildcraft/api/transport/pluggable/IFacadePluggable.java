package buildcraft.api.transport.pluggable;

import net.minecraft.block.Block;

public interface IFacadePluggable {
	Block getCurrentBlock();

	int getCurrentMetadata();

	boolean isTransparent();

	boolean isHollow();
}
