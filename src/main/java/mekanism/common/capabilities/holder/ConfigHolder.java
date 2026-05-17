package mekanism.common.capabilities.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class ConfigHolder<CONTAINER> implements IContainerHolder<CONTAINER> {

    private static final Predicate<ISlotInfo> CAN_INPUT = ISlotInfo::canInput;
    private static final Predicate<ISlotInfo> CAN_OUTPUT = ISlotInfo::canOutput;

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
        public boolean isEmpty() {
            return false;
        }

        @Override
        public String toString() {
            return "No Config";
        }
    };

    private final Map<Direction, ISlotInfo> cachedSlotInfo = new EnumMap<>(Direction.class);
    private final ISideConfiguration sideConfiguration;
    private final TransmissionType transmissionType;
    private final List<CONTAINER> containers = new ArrayList<>();
    @UnknownNullability
    private final Function<ISlotInfo, List<CONTAINER>> slotInfoParser;
    @Nullable
    private Direction lastDirection;

    @Nullable
    private ConfigInfo lazyConfig;
    private boolean retrievedConfig;

    public ConfigHolder(ISideConfiguration sideConfiguration, TransmissionType transmissionType, @UnknownNullability Function<ISlotInfo, List<CONTAINER>> slotInfoParser) {
        this.sideConfiguration = sideConfiguration;
        this.transmissionType = transmissionType;
        this.slotInfoParser = slotInfoParser;
    }

    void addContainer(@NotNull CONTAINER container) {
        containers.add(container);
    }

    @Override
    public boolean canInsert(@Nullable Direction side) {
        return canInteract(side, CAN_INPUT);
    }

    @Override
    public boolean canExtract(@Nullable Direction side) {
        return canInteract(side, CAN_OUTPUT);
    }

    private boolean canInteract(@Nullable Direction side, @NotNull Predicate<ISlotInfo> interactPredicate) {
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

    @NotNull
    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        if (side == null) {
            //If we want the internal, give all of our slots
            return containers;
        }
        ISlotInfo slotInfo = getSlotInfo(side);
        if (slotInfo == NO_CONFIG) {
            //If we don't have a config (most likely case is it hasn't been set up yet, or we don't support this type of data in our configuration), just return all
            return containers;
        } else if (slotInfo == null) {
            return Collections.emptyList();
        }
        return slotInfoParser.apply(slotInfo);
    }

    @Nullable
    private ISlotInfo getSlotInfo(Direction side) {
        Direction direction = sideConfiguration.getDirection();
        if (direction != lastDirection) {
            //Invalid entire cache and update what direction we had as last if our last direction doesn't match the one we currently are facing
            cachedSlotInfo.clear();
            lastDirection = direction;
        } else if (cachedSlotInfo.containsKey(side)) {
            //If the direction changed, we know it isn't cached so can skip checking
            return cachedSlotInfo.get(side);
        }
        if (!retrievedConfig) {
            retrievedConfig = true;
            TileComponentConfig config = sideConfiguration.getConfig();
            if (config != null) {
                lazyConfig = config.getConfig(transmissionType);
                if (lazyConfig != null) {
                    //If we haven't added a listener to our config yet add one to remove the cached info we have for that side
                    //Note: We know we haven't done this if we haven't retrieved the config yet, as we only retrieve the config a single time
                    config.addConfigChangeListener(transmissionType, cachedSlotInfo::remove);
                }
            }
        }
        ISlotInfo slotInfo;
        if (lazyConfig == null) {
            slotInfo = NO_CONFIG;
        } else {
            slotInfo = lazyConfig.getSlotInfo(RelativeSide.fromDirections(direction, side));
            if (slotInfo != null && !slotInfo.isEnabled()) {
                //If we have a slot info, but it is not actually enabled, just store it as null to avoid having to recheck if it is enabled later
                slotInfo = null;
            }
        }
        cachedSlotInfo.put(side, slotInfo);
        return slotInfo;
    }
}