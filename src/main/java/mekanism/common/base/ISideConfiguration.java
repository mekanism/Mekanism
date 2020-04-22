package mekanism.common.base;

import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.util.Direction;

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
    Direction getOrientation();

    /**
     * Gets this machine's ejector.
     *
     * @return this machine's ejector
     */
    TileComponentEjector getEjector();

    @Nullable
    default DataType getActiveDataType(Object container) {
        if (container instanceof IGasTank && getConfig().supports(TransmissionType.GAS)) {
            ConfigInfo info = getConfig().getConfig(TransmissionType.GAS);
            List<DataType> types = info.getDataTypeForContainer(container);
            return types.size() > 0 && types.size() < info.getSupportedDataTypes().size()-1 ? types.get(0) : null;
        } else if (container instanceof IExtendedFluidTank && getConfig().supports(TransmissionType.FLUID)) {
            ConfigInfo info = getConfig().getConfig(TransmissionType.FLUID);
            List<DataType> types = info.getDataTypeForContainer(container);
            return types.size() > 0 && types.size() < info.getSupportedDataTypes().size()-1 ? types.get(0) : null;
        } else if (container instanceof IInventorySlot && getConfig().supports(TransmissionType.ITEM)) {
            ConfigInfo info = getConfig().getConfig(TransmissionType.ITEM);
            List<DataType> types = info.getDataTypeForContainer(container);
            return types.size() > 0 && types.size() < info.getSupportedDataTypes().size()-1 ? types.get(0) : null;
        }
        return null;
    }
}