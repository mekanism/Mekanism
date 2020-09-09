/*package mekanism.common.integration.energy.fluxnetworks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import sonar.fluxnetworks.api.energy.FNEnergyCapability;

@ParametersAreNonnullByDefault
public class FNEnergyCompat implements IEnergyCompat {

    @Nonnull
    @Override
    public Capability<?> getCapability() {
        return FNEnergyCapability.FN_ENERGY_STORAGE;
    }

    @Override
    public boolean isMatchingCapability(@Nonnull Capability<?> capability) {
        if (Mekanism.hooks.FluxNetworksLoaded) {
            //Ensure we check that Flux networks is loaded before attempting to access their capability
            return capability == FNEnergyCapability.FN_ENERGY_STORAGE;
        }
        return false;
    }

    @Override
    public boolean isUsable() {
        return !MekanismConfig.general.blacklistForge.get() && Mekanism.hooks.FluxNetworksLoaded && !MekanismConfig.general.blacklistFluxNetworks.get();
    }

    @Override
    public boolean isCapabilityPresent(ICapabilityProvider provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, FNEnergyCapability.FN_ENERGY_STORAGE, side).isPresent();
    }

    @Nonnull
    @Override
    public LazyOptional<?> getHandlerAs(@Nonnull IStrictEnergyHandler handler) {
        return LazyOptional.of(() -> new FNIntegration(handler));
    }

    @Nonnull
    @Override
    public LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, FNEnergyCapability.FN_ENERGY_STORAGE, side).lazyMap(FNStrictEnergyHandler::new);
    }
}*/