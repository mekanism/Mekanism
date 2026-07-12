package mekanism.common.integration.computer;

import java.util.function.BooleanSupplier;
import mekanism.common.Mekanism;
import mekanism.common.integration.computer.computercraft.IComputerCraftHelper;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister.BlockEntityTypeBuilder;
import mekanism.common.tile.base.CapabilityTileEntity;

public class ComputerCapabilityHelper {

    //Note: Bounding capability is handled by a listener in the corresponding computer integration
    public static <TILE extends CapabilityTileEntity & IComputerTile> void addComputerCapabilities(BlockEntityTypeBuilder<TILE> builder, BooleanSupplier supportsComputer) {
        if (Mekanism.hooks.computerCraft.isLoaded() && IComputerCraftHelper.INSTANCE != null) {
            //If ComputerCraft is loaded add the capability for it
            IComputerCraftHelper.INSTANCE.addCapability(builder, supportsComputer);
        }
    }
}