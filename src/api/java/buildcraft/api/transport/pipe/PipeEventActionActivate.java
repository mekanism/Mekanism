package buildcraft.api.transport.pipe;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IStatementParameter;

public class PipeEventActionActivate extends PipeEvent {
    public final IAction action;
    public final IStatementParameter[] params;
    public final EnumPipePart part;

    public PipeEventActionActivate(IPipeHolder holder, IAction action, IStatementParameter[] params, EnumPipePart part) {
        super(holder);
        this.action = action;
        this.params = params;
        this.part = part;
    }
}
