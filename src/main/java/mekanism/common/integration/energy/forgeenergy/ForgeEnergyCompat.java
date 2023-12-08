package mekanism.common.integration.energy.forgeenergy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.energy.IEnergyStorage;

@NothingNullByDefault
public class ForgeEnergyCompat implements IEnergyCompat {

    @Override
    public MultiTypeCapability<IEnergyStorage> getCapability() {
        return Capabilities.ENERGY;
    }

    @Override
    public boolean isUsable() {
        return EnergyUnit.FORGE_ENERGY.isEnabled();
    }

    @Override
    public <OBJECT, CONTEXT> ICapabilityProvider<OBJECT, CONTEXT, IEnergyStorage> getProviderAs(ICapabilityProvider<OBJECT, CONTEXT, IStrictEnergyHandler> provider) {
        return (obj, ctx) -> {
            IStrictEnergyHandler handler = provider.getCapability(obj, ctx);
            return handler != null && isUsable() ? wrapStrictEnergyHandler(handler) : null;
        };
    }

    @Override
    public IEnergyStorage wrapStrictEnergyHandler(IStrictEnergyHandler handler) {
        return new ForgeEnergyIntegration(handler);
    }

    @Override
    public IStrictEnergyHandler wrapAsStrictEnergyHandler(Object handler) {
        return new ForgeStrictEnergyHandler((IEnergyStorage) handler);
    }
}