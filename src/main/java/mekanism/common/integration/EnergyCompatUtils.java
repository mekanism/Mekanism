package mekanism.common.integration;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.fluxnetworks.forgeenergy.FNIntegration;
import mekanism.common.integration.fluxnetworks.forgeenergy.FNStrictEnergyHandler;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.forgeenergy.ForgeStrictEnergyHandler;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import sonar.fluxnetworks.api.energy.FNEnergyCapability;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class EnergyCompatUtils {

    /**
     * Checks if it is a known and enabled energy capability
     */
    public static boolean isEnergyCapability(Capability<?> capability) {
        if (capability == null) {
            //Should never be the case, but is when a capability does not exist due to a mod not being loaded
            return false;
        } else if (capability == Capabilities.STRICT_ENERGY_CAPABILITY) {
            return true;
        } else if (capability == CapabilityEnergy.ENERGY) {
            return useForge();
        } else if (isFluxNetworksCapability(capability)) {
            return useFluxNetworks();
        }
        return false;
    }

    public static boolean hasStrictEnergyHandler(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && hasStrictEnergyHandler(stack, null);
    }

    public static boolean hasStrictEnergyHandler(TileEntity tile, Direction side) {
        return tile != null && tile.getWorld() != null && hasStrictEnergyHandler((ICapabilityProvider) tile, side);
    }

    private static boolean hasStrictEnergyHandler(ICapabilityProvider provider, Direction side) {
        //Keep the things as lazy so that we don't have to resolve anything when we are just checking for existence
        if (CapabilityUtils.getCapability(provider, Capabilities.STRICT_ENERGY_CAPABILITY, side).isPresent()) {
            return true;
        }
        if (useForge()) {
            if (CapabilityUtils.getCapability(provider, CapabilityEnergy.ENERGY, side).isPresent()) {
                return true;
            }
        }
        if (useFluxNetworks()) {
            if (CapabilityUtils.getCapability(provider, FNEnergyCapability.FN_ENERGY_STORAGE, side).isPresent()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static IStrictEnergyHandler getStrictEnergyHandler(@Nonnull ItemStack stack) {
        return stack.isEmpty() ? null : getStrictEnergyHandler(stack, null);
    }

    @Nullable
    public static IStrictEnergyHandler getStrictEnergyHandler(TileEntity tile, Direction side) {
        return tile == null || tile.getWorld() == null ? null : getStrictEnergyHandler((ICapabilityProvider) tile, side);
    }

    @Nullable
    private static IStrictEnergyHandler getStrictEnergyHandler(ICapabilityProvider provider, Direction side) {
        Optional<IStrictEnergyHandler> energyCap = MekanismUtils.toOptional(CapabilityUtils.getCapability(provider, Capabilities.STRICT_ENERGY_CAPABILITY, side));
        if (energyCap.isPresent()) {
            return energyCap.get();
        }
        if (useFluxNetworks()) {
            //Note: We check the FN capability above Forge's so that we allow it to use the higher throughput amount than FN
            Optional<IFNEnergyStorage> fnEnergyCap = MekanismUtils.toOptional(CapabilityUtils.getCapability(provider, FNEnergyCapability.FN_ENERGY_STORAGE, side));
            if (fnEnergyCap.isPresent()) {
                return new FNStrictEnergyHandler(fnEnergyCap.get());
            }
        }
        if (useForge()) {
            Optional<IEnergyStorage> forgeEnergyCap = MekanismUtils.toOptional(CapabilityUtils.getCapability(provider, CapabilityEnergy.ENERGY, side));
            if (forgeEnergyCap.isPresent()) {
                return new ForgeStrictEnergyHandler(forgeEnergyCap.get());
            }
        }
        return null;
    }

    /**
     * @apiNote It is expected that isEnergyCapability is called before calling this method
     */
    @Nonnull
    public static <T> LazyOptional<T> getEnergyCapability(Capability<T> capability, @Nonnull IStrictEnergyHandler handler) {
        //TODO: Cache the lazy optionals, and the wrapper objects
        if (capability == null) {
            //Should never be the case, but is when a capability does not exist due to a mod not being loaded
            return LazyOptional.empty();
        } else if (capability == Capabilities.STRICT_ENERGY_CAPABILITY) {
            return Capabilities.STRICT_ENERGY_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> handler));
        } else if (useForge() && capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.orEmpty(capability, LazyOptional.of(() -> new ForgeEnergyIntegration(handler)));
        } else if (useFluxNetworks() && isFluxNetworksCapability(capability)) {
            return getAsFluxNetworks(handler).cast();
        }
        return LazyOptional.empty();
    }

    /**
     * Whether or not Forge power should be used.
     *
     * @return if Forge power should be used
     */
    private static boolean useForge() {
        return !MekanismConfig.general.blacklistForge.get();
    }

    /**
     * Whether or not Flux Networks long integration should be used.
     *
     * @return if Flux Networks long integration should be used
     */
    private static boolean useFluxNetworks() {
        return useForge() && Mekanism.hooks.FluxNetworksLoaded && !MekanismConfig.general.blacklistFluxNetworks.get();
    }

    /**
     * Checks if flux networks is loaded and if it is, then checks if the capability matches. This is to ensure we don't have any class loading issues
     */
    private static boolean isFluxNetworksCapability(Capability<?> capability) {
        if (Mekanism.hooks.FluxNetworksLoaded) {
            //Ensure we check that Flux networks is loaded before attempting to access their capability
            return capability == FNEnergyCapability.FN_ENERGY_STORAGE;
        }
        return false;
    }

    private static LazyOptional<?> getAsFluxNetworks(@Nonnull IStrictEnergyHandler handler) {
        //Note: This is a little ugly but this extra method is necessary to ensure the supplier's type
        // does not get resolved unless flux networks is present
        return LazyOptional.of(() -> new FNIntegration(handler));
    }

    /**
     * Whether or not IC2 power should be used, taking into account whether or not it is installed or another mod is providing its API.
     *
     * @return if IC2 power should be used
     */
    private static boolean useIC2() {
        //TODO: IC2
        return Mekanism.hooks.IC2Loaded/* && EnergyNet.instance != null*/ && !MekanismConfig.general.blacklistIC2.get();
    }

    public enum EnergyType {
        FORGE(MekanismConfig.general.FROM_FORGE, MekanismConfig.general.TO_FORGE),
        EU(MekanismConfig.general.FROM_IC2, MekanismConfig.general.TO_IC2);

        private final FloatingLongSupplier fromSupplier;
        private final FloatingLongSupplier toSupplier;

        EnergyType(FloatingLongSupplier fromSupplier, FloatingLongSupplier toSupplier) {
            this.fromSupplier = fromSupplier;
            this.toSupplier = toSupplier;
        }

        public FloatingLong convertFrom(long energy) {
            return fromSupplier.get().multiply(energy);
        }

        public FloatingLong convertFrom(FloatingLong energy) {
            return energy.multiply(fromSupplier.get());
        }

        public int convertToAsInt(FloatingLong joules) {
            return convertToAsFloatingLong(joules).intValue();
        }

        public long convertToAsLong(FloatingLong joules) {
            return convertToAsFloatingLong(joules).longValue();
        }

        public FloatingLong convertToAsFloatingLong(FloatingLong joules) {
            return joules.multiply(toSupplier.get());
        }
    }
}