package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import java.util.function.BooleanSupplier;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.ComputerEnergyHelper;
import mekanism.common.integration.computer.ComputerFilterHelper;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.base.CapabilityTileEntity;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public class CCCapabilityHelper {

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
    public static <TILE extends CapabilityTileEntity & IComputerTile> void addCapability(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        builder.with(PeripheralCapability.get(), (ICapabilityProvider<? super TILE, @Nullable Direction, IPeripheral>) PROVIDER, supportsComputer);
    }

    public static void addBoundingComputerCapabilities(RegisterCapabilitiesEvent event) {
        TileEntityBoundingBlock.proxyCapability(event, PeripheralCapability.get());
    }

    public static void registerApis() {
        ComputerCraftAPI.registerAPIFactory(CCApiObject.create(ComputerEnergyHelper.class, "mekanismEnergyHelper"));
        ComputerCraftAPI.registerAPIFactory(CCApiObject.create(ComputerFilterHelper.class, "mekanismFilterHelper"));
    }
}
