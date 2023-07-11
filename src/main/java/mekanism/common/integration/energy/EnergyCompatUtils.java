package mekanism.common.integration.energy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.listener.ConfigBasedCachedSupplier;
import mekanism.common.config.value.CachedValue;
import mekanism.common.integration.energy.fluxnetworks.FNEnergyCompat;
import mekanism.common.integration.energy.forgeenergy.ForgeEnergyCompat;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyCompatUtils {

    private EnergyCompatUtils() {
    }

    private static final List<IEnergyCompat> energyCompats = List.of(
          //We always have our own energy capability as the first one we check
          new StrictEnergyCompat(),
          //Note: We check the Flux Networks capability above Forge's so that we allow it to use the higher throughput amount supported by Flux Networks
          new FNEnergyCompat(),
          new ForgeEnergyCompat()
    );

    //Default the list of enabled caps to our own energy capability
    private static Supplier<List<Capability<?>>> ENABLED_ENERGY_CAPS = () -> List.of(Capabilities.STRICT_ENERGY);

    /**
     * @apiNote For internal uses, only call this after mods have loaded so that we can properly assume all {@link IEnergyCompat#isUsable()} checks only depend on config
     * settings.
     */
    public static void initLoadedCache() {
        Set<CachedValue<?>> configs = new HashSet<>();
        for (IEnergyCompat energyCompat : energyCompats) {
            configs.addAll(energyCompat.getBackingConfigs());
        }
        ENABLED_ENERGY_CAPS = new ConfigBasedCachedSupplier<>(
              () -> energyCompats.stream().filter(IEnergyCompat::isUsable).<Capability<?>>map(IEnergyCompat::getCapability).toList(),
              configs.toArray(new CachedValue[0])
        );
    }

    public static List<IEnergyCompat> getCompats() {
        return energyCompats;
    }

    /**
     * Checks if it is a known and enabled energy capability
     */
    public static boolean isEnergyCapability(@NotNull Capability<?> capability) {
        //The capability may not be registered if the mod that adds it is not loaded. In which case we can just
        // short circuit and not check if
        if (capability.isRegistered()) {
            for (IEnergyCompat energyCompat : energyCompats) {
                //Note: We don't need to check if it is usable before checking if the capability matches as it is instance equality
                if (energyCompat.isMatchingCapability(capability)) {
                    return energyCompat.isUsable();
                }
            }
        }
        return false;
    }

    /**
     * Gets all enabled energy capability integrations.
     */
    public static List<Capability<?>> getEnabledEnergyCapabilities() {
        return ENABLED_ENERGY_CAPS.get();
    }

    private static boolean isTileValid(@Nullable BlockEntity tile) {
        return tile != null && !tile.isRemoved() && tile.hasLevel();
    }

    public static boolean hasStrictEnergyHandler(@NotNull ItemStack stack) {
        return !stack.isEmpty() && hasStrictEnergyHandler(stack, null);
    }

    public static boolean hasStrictEnergyHandler(@Nullable BlockEntity tile, Direction side) {
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

    @Nullable
    public static IStrictEnergyHandler getStrictEnergyHandler(@NotNull ItemStack stack) {
        return getLazyStrictEnergyHandler(stack).resolve().orElse(null);
    }

    @NotNull
    public static LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(@NotNull ItemStack stack) {
        return stack.isEmpty() ? LazyOptional.empty() : getLazyStrictEnergyHandler(stack, null);
    }

    @NotNull
    public static LazyOptional<IStrictEnergyHandler> getLazyStrictEnergyHandler(@Nullable BlockEntity tile, Direction side) {
        return isTileValid(tile) ? getLazyStrictEnergyHandler((ICapabilityProvider) tile, side) : LazyOptional.empty();
    }

    @NotNull
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
    @NotNull
    public static <T> LazyOptional<T> getEnergyCapability(@NotNull Capability<T> capability, @NotNull IStrictEnergyHandler handler) {
        //The capability may not be registered if the mod that adds it is not loaded. In which case we can just
        // short circuit and not check if
        if (capability.isRegistered()) {
            //Note: The methods that call this method cache the returned lazy optional properly
            for (IEnergyCompat energyCompat : energyCompats) {
                if (energyCompat.isUsable() && energyCompat.isMatchingCapability(capability)) {
                    //Note: This is a little ugly but this extra method ensures that the supplier's type does not get prematurely resolved
                    return energyCompat.getHandlerAs(handler).cast();
                }
            }
        }
        return LazyOptional.empty();
    }

    /**
     * Whether IC2 power should be used, taking into account whether it is installed or another mod is providing its API.
     *
     * @return if IC2 power should be used
     */
    public static boolean useIC2() {
        //TODO: IC2
        //Note: Use default value if called before configs are loaded. In general this should never happen, but third party mods may just call it regardless
        return false;//Mekanism.hooks.IC2Loaded/* && EnergyNet.instance != null*/ && !MekanismConfig.general.blacklistIC2.getOrDefault();
    }
}