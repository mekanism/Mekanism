package mekanism.common.tile.interfaces;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Implement this if your TileEntity is capable of being modified by a Configurator in it's 'modify' mode.
 *
 * @author AidanBrady
 */
//TODO: Make the main mekanism tile extend this directly?
public interface ISideConfiguration {

    /**
     * Gets the tile's configuration component.
     *
     * @return the tile's configuration component
     */
    TileComponentConfig getConfig();

    /**
     * Gets this machine's current orientation.
     *
     * @return machine's current orientation
     */
    Direction getDirection();

    /**
     * Gets this machine's ejector.
     *
     * @return this machine's ejector
     */
    TileComponentEjector getEjector();

    @Nullable
    default DataType getActiveDataType(Object container) {
        ConfigInfo info = null;
        TileComponentConfig config = getConfig();
        if (container instanceof IChemicalTank && config.supports(TransmissionType.CHEMICAL)) {
            info = config.getConfig(TransmissionType.CHEMICAL);
        } else if (container instanceof IExtendedFluidTank && config.supports(TransmissionType.FLUID)) {
            info = config.getConfig(TransmissionType.FLUID);
        } else if (container instanceof IInventorySlot && config.supports(TransmissionType.ITEM)) {
            info = config.getConfig(TransmissionType.ITEM);
        }
        if (info != null) {
            List<DataType> types = info.getDataTypeForContainer(container);
            int count = types.size();
            //Note: This really is checking that there are data types for container and that there is at most
            // as many data types as there are supported types (excluding the NONE) type. We just check < instead
            // of <= size - 1 to cut down slightly on the calculations
            if (count > 0 && count < info.getSupportedDataTypes().size()) {
                return types.getFirst();
            }
        }
        return null;
    }
}