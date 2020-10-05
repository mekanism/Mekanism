package mekanism.common.integration.energy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.energy.forgeenergy.ForgeEnergyCompat;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class EnergyCompatUtils {

    private EnergyCompatUtils() {
    }

    private static final List<IEnergyCompat> energyCompats = Collections.unmodifiableList(Arrays.asList(
          //We always have our own energy capability as the first one we check
          new StrictEnergyCompat(),
          //Note: We check the Flux Networks capability above Forge's so that we allow it to use the higher throughput amount supported by Flux Networks
          //new FNEnergyCompat(),//TODO - FluxNetworks
          new ForgeEnergyCompat()
    ));

    public static List<IEnergyCompat> getCompats() {
        return energyCompats;
    }

    /**
     * Checks if it is a known and enabled energy capability
     */
    public static boolean isEnergyCapability(Capability<?> capability) {
        if (capability == null) {
            //Should never be the case, but is when a capability does not exist due to a mod not being loaded
            return false;
        }
        for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.isMatchingCapability(capability)) {
                return energyCompat.isUsable();
            }
        }
        return false;
    }

    /**
     * Gets all enabled energy capability integrations.
     */
    public static List<Capability<?>> getEnabledEnergyCapabilities() {
        return energyCompats.stream().filter(IEnergyCompat::isUsable).map(IEnergyCompat::getCapability).collect(Collectors.toList());
    }

    private static boolean isTileValid(@Nullable TileEntity tile) {
        return tile != null && !tile.isRemoved() && tile.hasWorld();
    }

    public static boolean hasStrictEnergyHandler(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && hasStrictEnergyHandler(stack, null);
    }

    public static boolean hasStrictEnergyHandler(@Nullable TileEntity tile, Direction side) {
        return isTileValid(tile) && hasStrictEnergyHandler((ICapabilityProvider) tile, side);
    }

    private static boolean hasStrictEnergyHandler(ICapabilityProvider provider, Direction side) {
        //Keep the things as lazy so that we don't have to resolve anything when we are just checking for existence
        for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.isUsable() && energyCompat.isCapabilityPresent(provider, side)) {
                return true;
            }
        }
        return false;
    }

    @Nullable//TODO: Transition usages of this to getLazyStrictEnergyHandler?
    public static IStrictEnergyHandler getStrictEnergyHandler(@Nonnull ItemStack stack) {
        return getLazyStrictEnergyHandler(stack).resolve().orElse(null);
    }

    @Nonnull
    public static LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(@Nonnull ItemStack stack) {
        return stack.isEmpty() ? LazyOptional.empty() : getLazyStrictEnergyHandler(stack, null);
    }

    @Nonnull
    public static LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(@Nullable TileEntity tile, Direction side) {
        return isTileValid(tile) ? getLazyStrictEnergyHandler((ICapabilityProvider) tile, side) : LazyOptional.empty();
    }

    @Nonnull
    private static LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(ICapabilityProvider provider, Direction side) {
        //TODO: Eventually look into making it so that we cache the handler we get back. Maybe by passing a listener
        // to this that we can give to the capability as we wrap the result into
        for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.isUsable()) {
                LazyOptional<IStrictEnergyHandler> handler = energyCompat.getLazyStrictEnergyHandler(provider, side);
                if (handler.isPresent()) {
                    return handler;
                }
            }
        }
        return LazyOptional.empty();
    }

    /**
     * @apiNote It is expected that isEnergyCapability is called before calling this method
     */
    @Nonnull
    public static <T> LazyOptional<T> getEnergyCapability(Capability<T> capability, @Nonnull IStrictEnergyHandler handler) {
        if (capability == null) {
            //Should never be the case, but is when a capability does not exist due to a mod not being loaded
            return LazyOptional.empty();
        }
        //Note: The methods that call this method cache the returned lazy optional properly
        for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.isUsable() && energyCompat.isMatchingCapability(capability)) {
                //Note: This is a little ugly but this extra method ensures that the supplier's type does not get prematurely resolved
                return energyCompat.getHandlerAs(handler).cast();
            }
        }
        return LazyOptional.empty();
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