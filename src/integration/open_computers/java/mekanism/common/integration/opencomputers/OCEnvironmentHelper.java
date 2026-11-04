package mekanism.common.integration.opencomputers;

import java.util.function.BooleanSupplier;
import li.cil.oc.api.network.Environment;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.opencomputers.IOpenComputersHelper;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.base.CapabilityTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.Nullable;

public final class OCEnvironmentHelper implements IOpenComputersHelper {

    private static final BlockCapability<Environment, @Nullable Direction> ENVIRONMENT_CAPABILITY = BlockCapability.createSided(
          Identifier.fromNamespaceAndPath(MekanismHooks.OPEN_COMPUTERS_MOD_ID, "environment"), Environment.class);
    private static final ICapabilityProvider<?, @Nullable Direction, Environment> PROVIDER = getProvider();

    private static <TILE extends CapabilityTileEntity & IComputerTile> ICapabilityProvider<TILE, @Nullable Direction, Environment> getProvider() {
        return CapabilityTileEntity.capabilityProvider(ENVIRONMENT_CAPABILITY, (tile, cap) -> {
            if (tile.isComputerCapabilityPersistent()) {
                return BasicCapabilityResolver.persistent(cap, () -> new MekanismEnvironment<>(tile));
            }
            return BasicCapabilityResolver.create(cap, () -> new MekanismEnvironment<>(tile));
        });
    }

    private OCEnvironmentHelper() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TILE extends CapabilityTileEntity & IComputerTile> void addCapability(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        builder.with(ENVIRONMENT_CAPABILITY, (ICapabilityProvider<? super TILE, @Nullable Direction, Environment>) PROVIDER, supportsComputer);
    }

    static void addBoundingComputerCapabilities(RegisterCapabilitiesEvent event) {
        TileEntityBoundingBlock.proxyCapability(event, ENVIRONMENT_CAPABILITY);
    }
}