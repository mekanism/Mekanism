package mekanism.common.integration.energy.fluxnetworks;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@NothingNullByDefault
public class FNEnergyCompat implements IEnergyCompat {

    @Override
    public MultiTypeCapability<IFNEnergyStorage> getCapability() {
        return FNCapability.ENERGY;
    }

    @Override
    public boolean isUsable() {
        return capabilityExists() && isConfigEnabled();
    }

    private boolean isConfigEnabled() {
        return EnergyUnit.FORGE_ENERGY.isEnabled() && !MekanismConfig.general.blacklistFluxNetworks.getOrDefault();
    }

    @Override
    public boolean capabilityExists() {
        return Mekanism.hooks.fluxNetworks.isLoaded();
    }

    @Override
    public <OBJECT, CONTEXT> ICapabilityProvider<OBJECT, CONTEXT, ?> getProviderAs(ICapabilityProvider<OBJECT, CONTEXT, IStrictEnergyHandler> provider) {
        return (obj, ctx) -> {
            IStrictEnergyHandler handler = provider.getCapability(obj, ctx);
            return handler != null && isConfigEnabled() ? wrapStrictEnergyHandler(handler) : null;
        };
    }

    @Override
    public Object wrapStrictEnergyHandler(IStrictEnergyHandler handler) {
        return new FNIntegration(handler);
    }

    @Override
    public IStrictEnergyHandler wrapAsStrictEnergyHandler(Object handler) {
        return new FNStrictEnergyHandler((IFNEnergyStorage) handler);
    }
}