package mekanism.common.capabilities.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

public abstract class ConfigHolder<TYPE> implements IHolder {

    /**
     * Dummy ISlotInfo used for representing we have no config
     */
    private static final ISlotInfo NO_CONFIG = new ISlotInfo() {
        @Override
        public boolean canInput() {
            return true;
        }

        @Override
        public boolean canOutput() {
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public String toString() {
            return "No Config";
        }
    };

    private final Map<Direction, ISlotInfo> cachedSlotInfo = new EnumMap<>(Direction.class);
    private final Supplier<TileComponentConfig> configSupplier;
    private final Supplier<Direction> facingSupplier;
    protected final List<TYPE> slots = new ArrayList<>();
    @Nullable
    private Direction lastDirection;
    private boolean listenerAdded;

    protected ConfigHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        this.facingSupplier = facingSupplier;
        this.configSupplier = configSupplier;
    }

    protected abstract TransmissionType getTransmissionType();

    @Override
    public boolean canInsert(@Nullable Direction side) {
        return canInteract(side, ISlotInfo::canInput);
    }

    @Override
    public boolean canExtract(@Nullable Direction side) {
        return canInteract(side, ISlotInfo::canOutput);
    }

    private boolean canInteract(@Nullable Direction side, @Nonnull Predicate<ISlotInfo> interactPredicate) {
        if (side == null) {
            return false;
        }
        ISlotInfo slotInfo = getSlotInfo(side);
        if (slotInfo == NO_CONFIG) {
            //We don't have a config: allow interacting
            return true;
        }
        return slotInfo != null && interactPredicate.test(slotInfo);
    }

    @Nonnull
    protected List<TYPE> getSlots(@Nullable Direction side, @Nonnull Function<ISlotInfo, List<TYPE>> slotInfoParser) {
        if (side == null) {
            //If we want the internal, give all of our slots
            return slots;
        }
        ISlotInfo slotInfo = getSlotInfo(side);
        if (slotInfo == NO_CONFIG) {
            //If we don't have a config (most likely case is it hasn't been set up yet, or we don't support this type of data in our configuration), just return all
            return slots;
        } else if (slotInfo == null) {
            return Collections.emptyList();
        }
        return slotInfoParser.apply(slotInfo);
    }

    @Nullable
    private ISlotInfo getSlotInfo(Direction side) {
        Direction direction = facingSupplier.get();
        if (direction != lastDirection) {
            //Invalid entire cache and update what direction we had as last if our last direction doesn't match the one we currently are facing
            cachedSlotInfo.clear();
            lastDirection = direction;
        } else if (cachedSlotInfo.containsKey(side)) {
            return cachedSlotInfo.get(side);
        }
        ISlotInfo slotInfo;
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            slotInfo = NO_CONFIG;
        } else {
            TransmissionType transmissionType = getTransmissionType();
            ConfigInfo configInfo = config.getConfig(transmissionType);
            if (configInfo == null) {
                slotInfo = NO_CONFIG;
            } else {
                if (!listenerAdded) {
                    //If we haven't added a listener to our config yet add one to remove the cached info we have for that side
                    listenerAdded = true;
                    config.addConfigChangeListener(transmissionType, cachedSlotInfo::remove);
                }
                slotInfo = configInfo.getSlotInfo(RelativeSide.fromDirections(direction, side));
                if (slotInfo != null && !slotInfo.isEnabled()) {
                    //If we have a slot info, but it is not actually enabled, just store it as null to avoid having to recheck if it is enabled later
                    slotInfo = null;
                }
            }
        }
        cachedSlotInfo.put(side, slotInfo);
        return slotInfo;
    }
}