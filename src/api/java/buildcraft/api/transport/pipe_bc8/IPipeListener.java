package buildcraft.api.transport.pipe_bc8;

import buildcraft.api.core.INBTLoadable_BC8;
import buildcraft.api.core.INetworkLoadable_BC8;

/** Something that will be saved and loaded from disk and sent across the network to the client world to be added as a
 * listener. This class should be registered with {@link PipeAPI_BC8#PIPE_LISTENER_REGISTRY} in order to be successfully
 * saved and loaded. */
public interface IPipeListener extends INBTLoadable_BC8<IPipeListener>, INetworkLoadable_BC8<IPipeListener> {}
