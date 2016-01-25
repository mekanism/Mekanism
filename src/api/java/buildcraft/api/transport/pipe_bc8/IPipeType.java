package buildcraft.api.transport.pipe_bc8;

import java.util.Collections;
import java.util.List;

public interface IPipeType {
    /** Creates all of the default listeners for this pipe. Normally this will be a list with a single object, that will
     * normally be a pipe transport. However, you can use this to register any listeners you wish. */
    List<IPipeListener> createDefaultListeners(IPipe_BC8 pipe);

    enum Void implements IPipeType {
        STRUCTURE,
        POWER,
        FLUID,
        ITEM;

        @Override
        public List<IPipeListener> createDefaultListeners(IPipe_BC8 pipe) {
            return Collections.emptyList();
        }
    }
}
