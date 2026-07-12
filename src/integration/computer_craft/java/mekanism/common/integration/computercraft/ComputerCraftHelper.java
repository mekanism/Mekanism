package mekanism.common.integration.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import java.util.function.BooleanSupplier;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.computercraft.IComputerCraftHelper;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jspecify.annotations.Nullable;

public class ComputerCraftHelper implements IComputerCraftHelper {

    private static final ICapabilityProvider<?, @Nullable Direction, IPeripheral> PROVIDER = getProvider();

    private static <TILE extends CapabilityTileEntity & IComputerTile> ICapabilityProvider<TILE, @Nullable Direction, IPeripheral> getProvider() {
        return CapabilityTileEntity.capabilityProvider(PeripheralCapability.get(), (tile, cap) -> {
            if (tile.isComputerCapabilityPersistent()) {
                return BasicCapabilityResolver.persistent(cap, () -> MekanismPeripheral.create(tile));
            }
            return BasicCapabilityResolver.create(cap, () -> MekanismPeripheral.create(tile));
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TILE extends CapabilityTileEntity & IComputerTile> void addCapability(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        builder.with(PeripheralCapability.get(), (ICapabilityProvider<? super TILE, @Nullable Direction, IPeripheral>) PROVIDER, supportsComputer);
    }
}
