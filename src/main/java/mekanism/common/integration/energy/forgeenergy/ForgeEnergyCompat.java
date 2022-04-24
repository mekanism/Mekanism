package mekanism.common.integration.energy.forgeenergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

@ParametersAreNonnullByDefault
public class ForgeEnergyCompat implements IEnergyCompat {

    @Nonnull
    @Override
    public Capability<IEnergyStorage> getCapability() {
        return CapabilityEnergy.ENERGY;
    }

    @Override
    public boolean isUsable() {
        return EnergyUnit.FORGE_ENERGY.isEnabled();
    }

    @Nonnull
    @Override
    public LazyOptional<IEnergyStorage> getHandlerAs(@Nonnull IStrictEnergyHandler handler) {
        return LazyOptional.of(() -> new ForgeEnergyIntegration(handler));
    }

    @Nonnull
    @Override
    public LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, @Nullable Direction side) {
        return CapabilityUtils.getCapability(provider, getCapability(), side).lazyMap(ForgeStrictEnergyHandler::new);
    }
}