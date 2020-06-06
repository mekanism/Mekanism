package mekanism.common.tile.interfaces;

import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
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
        ConfigInfo info = null;
        if (container instanceof IGasTank && getConfig().supports(TransmissionType.GAS)) {
            info = getConfig().getConfig(TransmissionType.GAS);
        } else if (container instanceof IInfusionTank && getConfig().supports(TransmissionType.INFUSION)) {
            info = getConfig().getConfig(TransmissionType.INFUSION);
        } else if (container instanceof IPigmentTank && getConfig().supports(TransmissionType.PIGMENT)) {
            info = getConfig().getConfig(TransmissionType.PIGMENT);
        } else if (container instanceof ISlurryTank && getConfig().supports(TransmissionType.SLURRY)) {
            info = getConfig().getConfig(TransmissionType.SLURRY);
        } else if (container instanceof IExtendedFluidTank && getConfig().supports(TransmissionType.FLUID)) {
            info = getConfig().getConfig(TransmissionType.FLUID);
        } else if (container instanceof IInventorySlot && getConfig().supports(TransmissionType.ITEM)) {
            info = getConfig().getConfig(TransmissionType.ITEM);
        }
        if (info != null) {
            List<DataType> types = info.getDataTypeForContainer(container);
            int count = types.size();
            if (count > 0 && count < info.getSupportedDataTypes().size() - 1) {
                return types.get(0);
            }
        }
        return null;
    }
}