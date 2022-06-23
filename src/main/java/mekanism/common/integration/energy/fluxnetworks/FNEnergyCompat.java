package mekanism.common.integration.energy.fluxnetworks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@ParametersAreNonnullByDefault
public class FNEnergyCompat implements IEnergyCompat {

    private static final Capability<IFNEnergyStorage> FN_ENERGY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    @Nonnull
    @Override
    public Capability<?> getCapability() {
        return FN_ENERGY_CAPABILITY;
    }

    @Override
    public boolean isMatchingCapability(@Nonnull Capability<?> capability) {
        return capability == FN_ENERGY_CAPABILITY;
    }

    @Override
    public boolean isUsable() {
        return EnergyUnit.FORGE_ENERGY.isEnabled() && Mekanism.hooks.FluxNetworksLoaded && !MekanismConfig.general.blacklistFluxNetworks.get();
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