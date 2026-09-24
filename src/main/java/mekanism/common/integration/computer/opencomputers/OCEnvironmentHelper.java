package mekanism.common.integration.computer.opencomputers;

import java.util.function.BooleanSupplier;
import li.cil.oc.api.network.Environment;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.base.CapabilityTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public final class OCEnvironmentHelper {

    private static final BlockCapability<Environment, Direction> ENVIRONMENT_CAPABILITY = BlockCapability.createSided(
          ResourceLocation.fromNamespaceAndPath("opencomputers", "environment"), Environment.class);
    private static final ICapabilityProvider<?, @Nullable Direction, Environment> PROVIDER = getProvider();

    private static <TILE extends CapabilityTileEntity & IComputerTile>
    ICapabilityProvider<TILE, @Nullable Direction, Environment> getProvider() {
        return CapabilityTileEntity.capabilityProvider(ENVIRONMENT_CAPABILITY, (tile, cap) -> {
            if (tile.isComputerCapabilityPersistent()) {
                return BasicCapabilityResolver.persistent(cap, () -> new MekanismEnvironment<>(tile));
            }
            return BasicCapabilityResolver.create(cap, () -> new MekanismEnvironment<>(tile));
        });
    }

    private OCEnvironmentHelper() {
    }

    @SuppressWarnings("unchecked")
    public static <TILE extends CapabilityTileEntity & IComputerTile> void addCapability(
          BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        builder.with(ENVIRONMENT_CAPABILITY,
              (ICapabilityProvider<? super TILE, @Nullable Direction, Environment>) PROVIDER, supportsComputer);
    }

    public static void addBoundingComputerCapabilities(RegisterCapabilitiesEvent event) {
        TileEntityBoundingBlock.proxyCapability(event, ENVIRONMENT_CAPABILITY);
    }
}
