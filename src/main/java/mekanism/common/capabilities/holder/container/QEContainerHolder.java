package mekanism.common.capabilities.holder.container;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.common.capabilities.holder.QEConfigHolder;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class QEContainerHolder<CONTAINER> extends QEConfigHolder<List<CONTAINER>> implements IContainerHolder<CONTAINER> {

    public QEContainerHolder(TileEntityQuantumEntangloporter entangloporter, TransmissionType transmissionType, Function<ISlotInfo, List<CONTAINER>> slotInfoParser,
          Function<InventoryFrequency, List<CONTAINER>> containerResolver) {
        super(entangloporter, transmissionType, slotInfoParser, containerResolver);
    }

    @Override
    protected List<CONTAINER> defaultValue() {
        return Collections.emptyList();
    }

    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        return getData(side);
    }
}