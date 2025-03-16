package mekanism.common.integration.energy.grandpower;

import dev.technici4n.grandpower.api.ILongEnergyStorage;
import dev.technici4n.grandpower.impl.NonLongWrapper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class GPEnergyCompat implements IEnergyCompat {

    @Nullable
    private static final Class<?> NON_LONG_WRAPPER;

    static {
        Class<?> tmp;
        try {
            //noinspection UnstableApiUsage
            tmp = NonLongWrapper.class;
        } catch (NoClassDefFoundError e) {
            tmp = null;
        }
        NON_LONG_WRAPPER = tmp;
    }

    @Override
    public MultiTypeCapability<ILongEnergyStorage> getCapability() {
        return GPCapability.ENERGY;
    }

    @Override
    public boolean isUsable() {
        return capabilityExists() && isConfigEnabled();
    }

    private boolean isConfigEnabled() {
        return EnergyUnit.FORGE_ENERGY.isEnabled() && !MekanismConfig.general.blacklistGrandPower.getOrDefault();
    }

    @Override
    public boolean capabilityExists() {
        return Mekanism.hooks.grandPower.isLoaded();
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
        return new GPIntegration(handler);
    }

    @Override
    @Nullable
    public IStrictEnergyHandler wrapAsStrictEnergyHandler(Object handler) {
        if (NON_LONG_WRAPPER != null && NON_LONG_WRAPPER.isInstance(handler)) {
            return null;//prevent double wrapping of FE by GP
        }
        return new GPStrictEnergyHandler((ILongEnergyStorage) handler);
    }
}