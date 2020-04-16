package mekanism.common.tile.component.config;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;

public class ConfigInfo {

    private final Supplier<Direction> facingSupplier;
    //TODO: Ejecting/can eject, how do we want to use these
    //TODO: When can eject is false don't even show the auto eject button
    private boolean canEject;
    private boolean ejecting;
    private Map<RelativeSide, DataType> sideConfig;
    private Map<DataType, ISlotInfo> slotInfo;

    public ConfigInfo(@Nonnull Supplier<Direction> facingSupplier) {
        this.facingSupplier = facingSupplier;
        canEject = true;
        ejecting = false;
        sideConfig = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : EnumUtils.SIDES) {
            sideConfig.put(side, DataType.NONE);
        }
        slotInfo = new EnumMap<>(DataType.class);
    }

    private RelativeSide getSide(Direction direction) {
        return RelativeSide.fromDirections(facingSupplier.get(), direction);
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

    public void setDataType(@Nonnull RelativeSide side, @Nonnull DataType dataType) {
        sideConfig.put(side, dataType);
    }

    @Nonnull
    public Set<DataType> getSupportedDataTypes() {
        Set<DataType> dataTypes = EnumSet.of(DataType.NONE);
        dataTypes.addAll(slotInfo.keySet());
        return dataTypes;
    }

    public void fill(@Nonnull DataType dataType) {
        for (RelativeSide side : EnumUtils.SIDES) {
            setDataType(side, dataType);
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
    }

    public Set<Direction> getSidesForData(@Nonnull DataType dataType) {
        Direction facing = facingSupplier.get();
        return sideConfig.entrySet().stream().filter(entry -> entry.getValue().equals(dataType)).map(entry ->
              entry.getKey().getDirection(facing)).collect(Collectors.toCollection(() -> EnumSet.noneOf(Direction.class)));
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