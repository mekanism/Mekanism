package buildcraft.api.transport.pipe_bc8;

import java.util.Set;

import net.minecraft.nbt.NBTBase;

import buildcraft.api.core.INBTLoadable_BC8;
import buildcraft.api.core.INetworkLoadable_BC8;

public interface IPipePropertyProvider {
    /** This will return either the value of the property (if {@link #hasProperty(IPipeProperty)} returns true) or the
     * default value of the property. */
    <T> T getValue(IPipeProperty<T> property);

    boolean hasProperty(IPipeProperty<?> property);

    Set<IPipeProperty<?>> getPropertySet();

    /** Defines a property key- this should be stored in a publicly accessible static variable somewhere. */
    public interface IPipeProperty<T> {
        String getName();

        T getDefault();
    }

    /** Defines a pipe property that has its value queried every time it is asked for its value. */
    public interface IPipePropertyImplicit<T> extends IPipeProperty<T> {
        T getValue(IPipe_BC8 pipe);
    }

    /** Defines a pipe property that has a value explicitly set */
    public interface IPipePropertyValue<T> extends IPipeProperty<T>, INBTLoadable_BC8<T>, INetworkLoadable_BC8<T> {
        T getValue();

        @Override
        T readFromNBT(NBTBase tag);
    }

    /** Defines a provider that can have value properties changed and added. */
    public interface IPipePropertyProviderEditable extends IPipePropertyProvider, INBTLoadable_BC8<IPipePropertyProviderEditable>,
            INetworkLoadable_BC8<IPipePropertyProviderEditable> {
        <T> void addProperty(IPipePropertyValue<T> property);

        <T> void removeProperty(IPipePropertyValue<T> property);

        IPipePropertyProvider asReadOnly();
    }
}
