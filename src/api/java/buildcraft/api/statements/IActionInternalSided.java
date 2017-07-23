package buildcraft.api.statements;

import net.minecraft.util.EnumFacing;

public interface IActionInternalSided extends IAction {
    void actionActivate(EnumFacing side, IStatementContainer source, IStatementParameter[] parameters);
}
