package mekanism.common.integration.energy.forgeenergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

@ParametersAreNonnullByDefault
public class ForgeEnergyCompat implements IEnergyCompat {

    @Nonnull
    @Override
    public Capability<?> getCapability() {
        return CapabilityEnergy.ENERGY;
    }

    @Override
    public boolean isUsable() {
        return !MekanismConfig.general.blacklistForge.get();
    }

    @Override
    public boolean isCapabilityPresent(ICapabilityProvider provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, CapabilityEnergy.ENERGY, side).isPresent();
    }

    @Nonnull
    @Override
    public LazyOptional<IEnergyStorage> getHandlerAs(@Nonnull IStrictEnergyHandler handler) {
        return LazyOptional.of(() -> new ForgeEnergyIntegration(handler));
    }

    @Nonnull
    @Override
    public LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, CapabilityEnergy.ENERGY, side).lazyMap(ForgeStrictEnergyHandler::new);
    }
}