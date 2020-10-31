package mekanism.common.capabilities.holder;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
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
        return canInteract(direction, ISlotInfo::canInput);
    }

    @Override
    public boolean canExtract(@Nullable Direction direction) {
        return canInteract(direction, ISlotInfo::canOutput);
    }

    private boolean canInteract(@Nullable Direction direction, @Nonnull Predicate<ISlotInfo> interactPredicate) {
        if (direction == null) {
            return false;
        }
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            //If we don't have a config: allow interacting
            return true;
        }
        ConfigInfo configInfo = config.getConfig(getTransmissionType());
        if (configInfo == null) {
            //We don't have a config: allow interacting
            return true;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        ISlotInfo slotInfo = configInfo.getSlotInfo(side);
        return slotInfo != null && interactPredicate.test(slotInfo);
    }

    @Nonnull
    protected <TYPE> List<TYPE> getSlots(@Nullable Direction direction, @Nonnull List<TYPE> allSlots, @Nonnull Function<ISlotInfo, List<TYPE>> slotInfoParser) {
        if (direction == null) {
            //If we want the internal, give all of our slots
            return allSlots;
        }
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            //If we don't have a config (most likely case is it hasn't been setup yet), just return all slots
            return allSlots;
        }
        ConfigInfo configInfo = config.getConfig(getTransmissionType());
        if (configInfo == null) {
            //We don't support items in our configuration at all so just return all
            return allSlots;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        return slotInfoParser.apply(configInfo.getSlotInfo(side));
    }
}