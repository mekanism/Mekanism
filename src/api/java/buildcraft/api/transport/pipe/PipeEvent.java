package buildcraft.api.transport.pipe;

/** The base class for all pipe events. Some event classes can be cancelled with {@link #cancel()}, however this will
 * only have an effect if {@link #canBeCancelled} is true. Refer to individual classes for information on if they can
 * be cancelled, and what cancelling the event does. */
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

    /** Called after every event handler has received this pipe event, to pick up simple mistakes when implementing pipe
     * event handlers.
     *
     * @return Null if there are no state errors, or a message containing information about what is wrong (although this
     *         may be incomplete) */
    public String checkStateForErrors() {
        if (canceled & !canBeCancelled) {
            return "Somehow cancelled an event that isn't marked as such!";
        }
        return null;
    }
}
