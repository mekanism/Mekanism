package mekanism.common.integration.energy.fluxnetworks;

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
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@ParametersAreNonnullByDefault
public class FNEnergyCompat implements IEnergyCompat {

    @CapabilityInject(IFNEnergyStorage.class)
    private static Capability<IFNEnergyStorage> FN_ENERGY_CAPABILITY;

    @Nonnull
    @Override
    public Capability<?> getCapability() {
        return FN_ENERGY_CAPABILITY;
    }

    @Override
    public boolean isMatchingCapability(@Nonnull Capability<?> capability) {
        if (Mekanism.hooks.FluxNetworksLoaded) {
            //Ensure we check that Flux networks is loaded before attempting to access their capability
            return capability == FN_ENERGY_CAPABILITY;
        }
        return false;
    }

    @Override
    public boolean isUsable() {
        return !MekanismConfig.general.blacklistForge.get() && Mekanism.hooks.FluxNetworksLoaded && !MekanismConfig.general.blacklistFluxNetworks.get();
    }

    @Override
    public boolean isCapabilityPresent(ICapabilityProvider provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, FN_ENERGY_CAPABILITY, side).isPresent();
    }

    @Nonnull
    @Override
    public LazyOptional<?> getHandlerAs(@Nonnull IStrictEnergyHandler handler) {
        return LazyOptional.of(() -> new FNIntegration(handler));
    }

    @Nonnull
    @Override
    public LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, FN_ENERGY_CAPABILITY, side).lazyMap(FNStrictEnergyHandler::new);
    }
}