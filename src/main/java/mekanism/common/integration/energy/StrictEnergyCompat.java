package mekanism.common.integration.energy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

@NothingNullByDefault
public class StrictEnergyCompat implements IEnergyCompat {

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public MultiTypeCapability<IStrictEnergyHandler> getCapability() {
        return Capabilities.STRICT_ENERGY;
    }

    @Override
    public <OBJECT, CONTEXT> ICapabilityProvider<OBJECT, CONTEXT, IStrictEnergyHandler> getProviderAs(ICapabilityProvider<OBJECT, CONTEXT, IStrictEnergyHandler> provider) {
        return provider;
    }

    @Override
    public IStrictEnergyHandler wrapStrictEnergyHandler(IStrictEnergyHandler handler) {
        return handler;
    }

    @Override
    public IStrictEnergyHandler wrapAsStrictEnergyHandler(Object handler) {
        return (IStrictEnergyHandler) handler;
    }
}