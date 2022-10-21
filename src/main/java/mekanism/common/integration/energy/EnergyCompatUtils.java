package mekanism.common.integration.energy;

import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
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
        return energyCompats.stream().filter(IEnergyCompat::isUsable).map(IEnergyCompat::getCapability).collect(Collectors.toList());
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

    @Nullable//TODO: Transition usages of this to getLazyStrictEnergyHandler?
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
        return Mekanism.hooks.IC2Loaded/* && EnergyNet.instance != null*/ && !MekanismConfig.general.blacklistIC2.getOrDefault();
    }
}