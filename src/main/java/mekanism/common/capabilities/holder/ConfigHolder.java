package mekanism.common.capabilities.holder;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.util.Direction;

public abstract class ConfigHolder implements IHolder {

    protected final Supplier<TileComponentConfig> configSupplier;
    protected final Supplier<Direction> facingSupplier;

    protected ConfigHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        this.facingSupplier = facingSupplier;
        this.configSupplier = configSupplier;
    }

    protected abstract TransmissionType getTransmissionType();

    @Override
    public boolean canInsert(@Nullable Direction direction) {
        if (direction == null) {
            return false;
        }
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            //If we don't have a config: allow inserting
            return true;
        }
        ConfigInfo configInfo = config.getConfig(getTransmissionType());
        if (configInfo == null) {
            //We don't have a gas config: allow inserting
            return true;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        ISlotInfo slotInfo = configInfo.getSlotInfo(side);
        return slotInfo != null && slotInfo.canInput();
    }

    @Override
    public boolean canExtract(@Nullable Direction direction) {
        if (direction == null) {
            return false;
        }
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            //If we don't have a config: allow extracting
            return true;
        }
        ConfigInfo configInfo = config.getConfig(getTransmissionType());
        if (configInfo == null) {
            //We don't have a gas config: allow extracting
            return true;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        ISlotInfo slotInfo = configInfo.getSlotInfo(side);
        return slotInfo != null && slotInfo.canOutput();
    }
}