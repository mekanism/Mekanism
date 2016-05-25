package buildcraft.api.transport.pipe_bc8.event_bc8;

import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyImplicit;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;

public interface IPipeEvent_BC8 {
    IPipe_BC8 getPipe();

    public interface Tick extends IPipeEvent_BC8 {
        long getCurrentTick();

        public interface Client extends Tick {}

        public interface Server extends Tick {}
    }

    public interface PropertyQuery<T> extends IPipeEvent_BC8 {
        Class<T> getTypeClass();

        IPipePropertyImplicit<T> getProperty();

        T getValue();

        void setValue(T newValue);
    }
}
