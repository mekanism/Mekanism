package buildcraft.api.statements;

import net.minecraft.util.EnumFacing;

public interface ITriggerInternalSided extends ITrigger {
    boolean isTriggerActive(EnumFacing side, IStatementContainer source, IStatementParameter[] parameters);
}
