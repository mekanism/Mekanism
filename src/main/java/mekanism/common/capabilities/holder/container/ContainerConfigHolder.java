package mekanism.common.capabilities.holder.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class ContainerConfigHolder<CONTAINER> extends ConfigHolder<List<CONTAINER>> implements IContainerHolder<CONTAINER> {

    private final List<CONTAINER> containers = new ArrayList<>();

    public ContainerConfigHolder(ISideConfiguration sideConfiguration, TransmissionType transmissionType, Function<ISlotInfo, List<CONTAINER>> slotInfoParser) {
        super(sideConfiguration, transmissionType, slotInfoParser);
    }

    void addContainer(CONTAINER container) {
        containers.add(container);
    }

    @Override
    protected List<CONTAINER> defaultValue() {
        return Collections.emptyList();
    }

    @Override
    protected List<CONTAINER> allData() {
        return containers;
    }

    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        return getData(side);
    }
}