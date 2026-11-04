package mekanism.common.integration.computer.opencomputers;

import java.util.function.BooleanSupplier;
import mekanism.api.MekanismAPI;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;
import org.jspecify.annotations.Nullable;

public interface IOpenComputersHelper {

    @Nullable
    IOpenComputersHelper INSTANCE = MekanismAPI.getOptionalService(IOpenComputersHelper.class);

    <TILE extends CapabilityTileEntity & IComputerTile> void addCapability(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer);
}