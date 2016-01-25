package buildcraft.api.transport.pipe_bc8;

import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyImplicit;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyValue;

public interface IPropertyRegistry {
    <T> IPipePropertyValue<T> getValueProperty(String modId, String name);

    <T> IPipePropertyValue<T> registerValueProperty(String name, Class<T> typeClass);

    <T> IPipePropertyImplicit<T> getImplicitProperty(String modId, String uniqueName);

    <T> IPipePropertyImplicit<T> registerSimpleImplicitProperty(String name, Class<T> typeClass);

    <T> IPipePropertyImplicit<T> registerCutomImplicitProperty(String name, IPipePropertyImplicit<T> property);

    // Used in case BuildCraft is not installed
    enum Void implements IPropertyRegistry {
        INSTANCE;

        // @formatter:off
        @Override
        public <T> IPipePropertyValue<T> getValueProperty(String a, String b) {
            return null;
        }

        @Override
        public <T> IPipePropertyValue<T> registerValueProperty(String a, Class<T> b) {
            return null;
        }

        @Override
        public <T> IPipePropertyImplicit<T> getImplicitProperty(String a, String b) {
            return null;
        }

        @Override
        public <T> IPipePropertyImplicit<T> registerSimpleImplicitProperty(String a, Class<T> b) {
            return null;
        }

        @Override
        public <T> IPipePropertyImplicit<T> registerCutomImplicitProperty(String a, IPipePropertyImplicit<T> p) {
            return p;
        }
        // @formatter:on
    }
}
