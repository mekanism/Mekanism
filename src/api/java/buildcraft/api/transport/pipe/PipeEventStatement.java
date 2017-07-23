package buildcraft.api.transport.pipe;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;

public abstract class PipeEventStatement extends PipeEvent {
    public PipeEventStatement(IPipeHolder holder) {
        super(holder);
    }

    /** Fired when a pipe should add internal triggers to the list of all possible triggers */
    public static class AddTriggerInternal extends PipeEventStatement {
        public final Collection<ITriggerInternal> triggers;

        public AddTriggerInternal(IPipeHolder holder, Collection<ITriggerInternal> triggers) {
            super(holder);
            this.triggers = triggers;
        }
    }

    /** Fired when a pipe should add internal sided triggers to the list of all possible triggers */
    public static class AddTriggerInternalSided extends PipeEventStatement {
        public final Collection<ITriggerInternalSided> triggers;

        @Nonnull
        public final EnumFacing side;

        public AddTriggerInternalSided(IPipeHolder holder, Collection<ITriggerInternalSided> triggers, @Nonnull EnumFacing side) {
            super(holder);
            this.triggers = triggers;
            this.side = side;
        }
    }

    /** Fired when a pipe should add internal actions to the list of all possible actions */
    public static class AddActionInternal extends PipeEventStatement {
        public final Collection<IActionInternal> actions;

        public AddActionInternal(IPipeHolder holder, Collection<IActionInternal> actions) {
            super(holder);
            this.actions = actions;
        }
    }

    /** Fired when a pipe should add internal actions to the list of all possible actions */
    public static class AddActionInternalSided extends PipeEventStatement {
        public final Collection<IActionInternalSided> actions;

        @Nonnull
        public final EnumFacing side;

        public AddActionInternalSided(IPipeHolder holder, Collection<IActionInternalSided> actions, @Nonnull EnumFacing side) {
            super(holder);
            this.actions = actions;
            this.side = side;
        }
    }
}
