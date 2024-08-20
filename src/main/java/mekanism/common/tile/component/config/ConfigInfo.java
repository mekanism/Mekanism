package mekanism.common.tile.component.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.EnumUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigInfo implements IPersistentConfigInfo {

    //TODO: Ejecting/can eject, how do we want to use these
    private boolean canEject;
    private boolean ejecting;
    private final Map<RelativeSide, DataType> sideConfig;
    private final Map<DataType, ISlotInfo> slotInfo;
    // used so slot & tank GUIs can quickly reference which color overlay to render
    private final Map<Object, List<DataType>> containerTypeMapping;
    //Not final so that it can be lazily initialized
    @Nullable
    private Set<RelativeSide> disabledSides;
    @Nullable
    private Set<DataType> supportedDataTypes;

    public ConfigInfo() {
        canEject = true;
        ejecting = false;
        sideConfig = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : EnumUtils.SIDES) {
            sideConfig.put(side, DataType.NONE);
        }
        slotInfo = new EnumMap<>(DataType.class);
        containerTypeMapping = new HashMap<>();
    }

    public boolean canEject() {
        return canEject;
    }

    public void setCanEject(boolean canEject) {
        this.canEject = canEject;
    }

    @Override
    public boolean isEjecting() {
        return ejecting;
    }

    public void setEjecting(boolean ejecting) {
        this.ejecting = ejecting;
    }

    public void addDisabledSides(@NotNull RelativeSide... sides) {
        if (disabledSides == null) {
            disabledSides = EnumSet.noneOf(RelativeSide.class);
        }
        for (RelativeSide side : sides) {
            disabledSides.add(side);
            sideConfig.put(side, DataType.NONE);
        }
    }

    public boolean isSideEnabled(@NotNull RelativeSide side) {
        if (disabledSides == null) {
            return true;
        }
        return !disabledSides.contains(side);
    }

    @NotNull
    @Override
    public DataType getDataType(@NotNull RelativeSide side) {
        return sideConfig.get(side);
    }

    public Set<Map.Entry<RelativeSide, DataType>> getSideConfig() {
        return sideConfig.entrySet();
    }

    public boolean setDataType(@NotNull DataType dataType, @NotNull RelativeSide side) {
        return isSideEnabled(side) && sideConfig.put(side, dataType) != dataType;
    }

    @NotNull
    public Set<DataType> getSupportedDataTypes() {
        if (supportedDataTypes == null) {
            supportedDataTypes = EnumSet.of(DataType.NONE);
            supportedDataTypes.addAll(slotInfo.keySet());
        }
        return supportedDataTypes;
    }

    public boolean supports(DataType type) {
        return type == DataType.NONE || slotInfo.containsKey(type);
    }

    @Nullable
    public ISlotInfo getSlotInfo(@NotNull RelativeSide side) {
        return getSlotInfo(getDataType(side));
    }

    @Nullable
    public ISlotInfo getSlotInfo(@NotNull DataType dataType) {
        return slotInfo.get(dataType);
    }

    public void addSlotInfo(@NotNull DataType dataType, @NotNull ISlotInfo info) {
        slotInfo.put(dataType, info);
        if (supportedDataTypes != null) {
            supportedDataTypes.add(dataType);
        }
        // set up mapping
        switch (info) {
            case ChemicalSlotInfo chemicalSlotInfo -> {
                for (IChemicalTank tank : chemicalSlotInfo.getTanks()) {
                    containerTypeMapping.computeIfAbsent(tank, t -> new ArrayList<>()).add(dataType);
                }
            }
            case FluidSlotInfo fluidSlotInfo -> {
                for (IExtendedFluidTank tank : fluidSlotInfo.getTanks()) {
                    containerTypeMapping.computeIfAbsent(tank, t -> new ArrayList<>()).add(dataType);
                }
            }
            case InventorySlotInfo inventorySlotInfo -> {
                for (IInventorySlot slot : inventorySlotInfo.getSlots()) {
                    containerTypeMapping.computeIfAbsent(slot, t -> new ArrayList<>()).add(dataType);
                }
            }
            default -> {
            }
        }
    }

    public List<DataType> getDataTypeForContainer(Object container) {
        return containerTypeMapping.getOrDefault(container, new ArrayList<>());
    }

    /**
     * @return The new data type
     */
    @NotNull
    public DataType incrementDataType(@NotNull RelativeSide relativeSide) {
        DataType current = getDataType(relativeSide);
        if (isSideEnabled(relativeSide)) {
            DataType newType = current.getNext(this::supports);
            sideConfig.put(relativeSide, newType);
            return newType;
        }
        return current;
    }

    /**
     * @return The new data type
     */
    @NotNull
    public DataType decrementDataType(@NotNull RelativeSide relativeSide) {
        DataType current = getDataType(relativeSide);
        if (isSideEnabled(relativeSide)) {
            DataType newType = current.getPrevious(this::supports);
            sideConfig.put(relativeSide, newType);
            return newType;
        }
        return current;
    }
}