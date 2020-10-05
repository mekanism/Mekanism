package mekanism.common.tile.component.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;

public class ConfigInfo {

    private final Supplier<Direction> facingSupplier;
    //TODO: Ejecting/can eject, how do we want to use these
    //TODO: When can eject is false don't even show the auto eject button
    private boolean canEject;
    private boolean ejecting;
    private final Map<RelativeSide, DataType> sideConfig;
    private final Map<DataType, ISlotInfo> slotInfo;
    // used so slot & tank GUIs can quickly reference which color overlay to render
    private final Map<Object, List<DataType>> containerTypeMapping;

    public ConfigInfo(@Nonnull Supplier<Direction> facingSupplier) {
        this.facingSupplier = facingSupplier;
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

    public boolean isEjecting() {
        return ejecting;
    }

    public void setEjecting(boolean ejecting) {
        this.ejecting = ejecting;
    }

    @Nonnull
    public DataType getDataType(@Nonnull RelativeSide side) {
        return sideConfig.get(side);
    }

    public void setDataType(@Nonnull DataType dataType, @Nonnull RelativeSide... sides) {
        for (RelativeSide side : sides) {
            sideConfig.put(side, dataType);
        }
    }

    @Nonnull
    public Set<DataType> getSupportedDataTypes() {
        Set<DataType> dataTypes = EnumSet.of(DataType.NONE);
        dataTypes.addAll(slotInfo.keySet());
        return dataTypes;
    }

    public void fill(@Nonnull DataType dataType) {
        for (RelativeSide side : EnumUtils.SIDES) {
            setDataType(dataType, side);
        }
    }

    @Nullable
    public ISlotInfo getSlotInfo(@Nonnull RelativeSide side) {
        return getSlotInfo(getDataType(side));
    }

    @Nullable
    public ISlotInfo getSlotInfo(@Nonnull DataType dataType) {
        return slotInfo.get(dataType);
    }

    public void addSlotInfo(@Nonnull DataType dataType, @Nonnull ISlotInfo info) {
        slotInfo.put(dataType, info);
        // set up mapping
        if (info instanceof ChemicalSlotInfo) {
            for (IChemicalTank<?, ?> tank : ((ChemicalSlotInfo<?, ?, ?>) info).getTanks()) {
                containerTypeMapping.computeIfAbsent(tank, t -> new ArrayList<>()).add(dataType);
            }
        } else if (info instanceof FluidSlotInfo) {
            for (IExtendedFluidTank tank : ((FluidSlotInfo) info).getTanks()) {
                containerTypeMapping.computeIfAbsent(tank, t -> new ArrayList<>()).add(dataType);
            }
        } else if (info instanceof InventorySlotInfo) {
            for (IInventorySlot slot : ((InventorySlotInfo) info).getSlots()) {
                containerTypeMapping.computeIfAbsent(slot, t -> new ArrayList<>()).add(dataType);
            }
        }
    }

    public List<DataType> getDataTypeForContainer(Object container) {
        return containerTypeMapping.getOrDefault(container, new ArrayList<>());
    }

    public void setDefaults() {
        if (slotInfo.containsKey(DataType.INPUT)) {
            fill(DataType.INPUT);
        }
        if (slotInfo.containsKey(DataType.OUTPUT)) {
            setDataType(DataType.OUTPUT, RelativeSide.RIGHT);
        }
        if (slotInfo.containsKey(DataType.EXTRA)) {
            setDataType(DataType.EXTRA, RelativeSide.BOTTOM);
        }
        if (slotInfo.containsKey(DataType.ENERGY)) {
            setDataType(DataType.ENERGY, RelativeSide.BACK);
        }
    }

    public Set<Direction> getSidesForData(@Nonnull DataType dataType) {
        return getSides(type -> type.equals(dataType));
    }

    public Set<Direction> getSides(Predicate<DataType> predicate) {
        Direction facing = facingSupplier.get();
        return sideConfig.entrySet().stream().filter(entry -> predicate.test(entry.getValue())).map(entry ->
              entry.getKey().getDirection(facing)).collect(Collectors.toCollection(() -> EnumSet.noneOf(Direction.class)));
    }

    public Set<Direction> getAllOutputtingSides() {
        return getSides(DataType::canOutput);
    }

    public Set<Direction> getSidesForOutput(DataType outputType) {
        return getSides(type -> type.equals(outputType) || type.equals(DataType.INPUT_OUTPUT));
    }

    /**
     * @return The new data type
     */
    @Nonnull
    public DataType incrementDataType(@Nonnull RelativeSide relativeSide) {
        Set<DataType> supportedDataTypes = getSupportedDataTypes();
        DataType newType = getDataType(relativeSide).getNext(supportedDataTypes::contains);
        sideConfig.put(relativeSide, newType);
        return newType;
    }

    /**
     * @return The new data type
     */
    @Nonnull
    public DataType decrementDataType(@Nonnull RelativeSide relativeSide) {
        Set<DataType> supportedDataTypes = getSupportedDataTypes();
        DataType newType = getDataType(relativeSide).getPrevious(supportedDataTypes::contains);
        sideConfig.put(relativeSide, newType);
        return newType;
    }
}