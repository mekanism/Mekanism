package buildcraft.api.statements.containers;

import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IStatementContainer;

/** Created by asie on 3/14/15. */
public interface ISidedStatementContainer extends IStatementContainer {
    EnumFacing getSide();
}
