package mekanism.common.integration.energy;

import java.util.List;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.energy.fluxnetworks.FNEnergyCompat;
import mekanism.common.integration.energy.forgeenergy.ForgeEnergyCompat;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
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

    //Default the list of enabled caps to our own energy capability and Neo's
    private static List<BlockCapability<?, @Nullable Direction>> LOADED_ENERGY_CAPS = List.of(Capabilities.STRICT_ENERGY.block(), Capabilities.ENERGY.block());

    /**
     * @apiNote For internal uses, only call this after mods have loaded so that we can properly assume all {@link IEnergyCompat#capabilityExists()} are present.
     */
    public static void initLoadedCache() {
        LOADED_ENERGY_CAPS = energyCompats.stream().filter(IEnergyCompat::capabilityExists).<BlockCapability<?, @Nullable Direction>>map(compat -> compat.getCapability().block()).toList();
    }

    public static List<IEnergyCompat> getCompats() {
        return energyCompats;
    }

    /**
     * Checks if it is a known and enabled energy capability
     */
    public static boolean isEnergyCapability(@NotNull BlockCapability<?, @Nullable Direction> capability) {
        for (IEnergyCompat energyCompat : energyCompats) {
            //TODO - 1.20.2: TEST THIS
            if (energyCompat.capabilityExists() && energyCompat.getCapability().block() == capability) {
                //TODO - 1.20.2: Should we check this before comparing instead of checking capability exists
                return energyCompat.isUsable();
            }
        }
        return false;
    }

    public static List<BlockCapability<?, @Nullable Direction>> getLoadedEnergyCapabilities() {
        return LOADED_ENERGY_CAPS;
    }

    public static void registerItemCapabilities(RegisterCapabilitiesEvent event, Item item, ICapabilityProvider<ItemStack, Void, IStrictEnergyHandler> mekProvider) {
        for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.capabilityExists()) {
                register(event, energyCompat.getCapability().item(), energyCompat.getProviderAs(mekProvider), item);
            }
        }
    }

    //Note: This extra method is required so that the code can compile even though inlining without the cast doesn't display any errors until attempting to compile
    @SuppressWarnings("unchecked")
    private static <CAP> void register(RegisterCapabilitiesEvent event, ItemCapability<CAP, Void> capability, ICapabilityProvider<ItemStack, Void, ?> provider, Item item) {
        event.registerItem(capability, (ICapabilityProvider<ItemStack, Void, CAP>) provider, item);
    }

    //TODO - 1.20.2: Should this use the block entity type or the block?
    //TODO: CALL THIS???? Though do we actually want to because we want to cache the wrapper object we provide... So we need to rethink this
    public static void addBlockCapabilities(BlockEntityTypeBuilder<? extends CapabilityTileEntity> builder) {
        for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.capabilityExists()) {
                //TODO: Figure out if we are better off using something like the below similar to what we do for items
                //register(event, energyCompat.getCapability().block(), type, energyCompat.getProviderAs(mekProvider));
                addCapability(builder, energyCompat.getCapability().block());
            }
        }
    }

    private static <CAP> void addCapability(BlockEntityTypeBuilder<? extends CapabilityTileEntity> builder, BlockCapability<CAP, @Nullable Direction> capability) {
        builder.with(capability, CapabilityTileEntity.basicCapabilityProvider(capability));
    }

    @Nullable
    public static Object wrapStrictEnergyHandler(BlockCapability<?, @Nullable Direction> capability, IStrictEnergyHandler handler) {
        for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.isUsable() && energyCompat.getCapability().block() == capability) {
                return energyCompat.wrapStrictEnergyHandler(handler);
            }
        }
        return null;
    }

    public static boolean hasStrictEnergyHandler(@NotNull ItemStack stack) {
        //TODO - 1.20.2: Evaluate usages and see if any can just keep hold of the handler
        // Also should we skip the wrapping to strict energy handler?
        return getStrictEnergyHandler(stack) != null;
    }

    @Nullable
    public static IStrictEnergyHandler getStrictEnergyHandler(@NotNull ItemStack stack) {
        if (!stack.isEmpty()) {
            for (IEnergyCompat energyCompat : energyCompats) {
                if (energyCompat.isUsable()) {
                    IStrictEnergyHandler handler = energyCompat.getStrictEnergyHandler(stack);
                    if (handler != null) {
                        return handler;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static IStrictEnergyHandler getStrictEnergyHandler(Level level, BlockPos pos, Direction side) {
        //TODO: Eventually look into making it so that we cache the handler we get back. Maybe by passing a listener
        // to this that we can give to the capability as we wrap the result into
        for (IEnergyCompat energyCompat : energyCompats) {
            if (energyCompat.isUsable()) {
                IStrictEnergyHandler handler = energyCompat.getAsStrictEnergyHandler(level, pos, side);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
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