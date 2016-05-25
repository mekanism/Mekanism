package buildcraft.api.transport.pipe_bc8;

import java.util.Map;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.core.INBTLoadable_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8;

/** Represents a pipe in a world. Most older (BC version less than 8.x) events, functions and querys can be called by
 * firing the appropriate event, and inspecting the event afterwards. Most functionality has been delegated to */
/* Note that this does not have a "get pipe tile" method, as pipes are not bound to just tile entities */
public interface IPipe_BC8 extends INBTLoadable_BC8<IPipe_BC8> {
    World getWorld();

    IPipeHolder_BC8 getHolder();

    IPipePropertyProvider getProperties();

    PipeBehaviour_BC8 getBehaviour();

    Map<EnumFacing, ? extends IConnection_BC8> getConnections();

    void fireEvent(IPipeEvent_BC8 event);

    /** Adds an event listener that will be saved and sent over the network.
     * 
     * @return True if the listener was added to the listener bus. If this returns false it will usually be todo with
     *         the listener not being allowed onto the pipe (for example it was a gate and there was already a gate on
     *         that side, or a facade or something similar to that) */
    boolean addEventListener(IPipeListener list);

    void removeEventListener(IPipeListener list);

    /** Sends all of the network data about this listener to the client. Note that this is called automatically by
     * {@link #addEventListener(IPipeListener)}. */
    void sendClientUpdate(IPipeListener listener);

    /** Makes this pipe send a client update at some point in the future. This might send it now, or it might send it
     * next tick. */
    void scheduleClientUpdate(IPipeListener listener);

    /** Makes this pipe re-render its static parts. */
    void sendRenderUpdate();

    /** Makes this pipe send a render update at some point in the future. This might send it now, or it might send it in
     * a few ticks time. */
    void scheduleRenderUpdate();
}
