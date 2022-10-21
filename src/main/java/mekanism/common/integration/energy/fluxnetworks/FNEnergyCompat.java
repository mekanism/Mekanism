package mekanism.common.integration.energy.fluxnetworks;

import mekanism.api.annotations.NothingNullByDefault;
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
import org.jetbrains.annotations.Nullable;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

@NothingNullByDefault
public class FNEnergyCompat implements IEnergyCompat {

    private static final Capability<IFNEnergyStorage> FN_ENERGY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    @Override
    public Capability<?> getCapability() {
        return FN_ENERGY_CAPABILITY;
    }

    @Override
    public boolean isMatchingCapability(Capability<?> capability) {
        return capability == FN_ENERGY_CAPABILITY;
    }

    @Override
    public boolean isUsable() {
        return EnergyUnit.FORGE_ENERGY.isEnabled() && Mekanism.hooks.FluxNetworksLoaded && !MekanismConfig.general.blacklistFluxNetworks.get();
    }

    @Override
    public LazyOptional<?> getHandlerAs(IStrictEnergyHandler handler) {
        return LazyOptional.of(() -> new FNIntegration(handler));
    }

    @Override
    public LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, FN_ENERGY_CAPABILITY, side).lazyMap(FNStrictEnergyHandler::new);
    }
}