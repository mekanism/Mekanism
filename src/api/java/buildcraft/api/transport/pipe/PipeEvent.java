package buildcraft.api.transport.pipe;

/** The base class for all pipe events. Some event classes can be cancelled with {@link #cancel()}, however this will
 * only have an effect if {@link #canBeCancelled} is true. Refer to indervidual classes for information on what*/
public abstract class PipeEvent {
    public final boolean canBeCancelled;
    public final IPipeHolder holder;
    private boolean canceled = false;

    public PipeEvent(IPipeHolder holder) {
        this(false, holder);
    }

    protected PipeEvent(boolean canBeCancelled, IPipeHolder holder) {
        this.canBeCancelled = canBeCancelled;
        this.holder = holder;
    }

    public void cancel() {
        if (canBeCancelled) {
            canceled = true;
        }
    }

    public boolean isCanceled() {
        return canceled;
    }
}
