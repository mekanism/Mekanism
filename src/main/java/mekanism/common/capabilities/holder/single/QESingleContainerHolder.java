package mekanism.common.capabilities.holder.single;

import java.util.function.Function;
import mekanism.common.capabilities.holder.QEConfigHolder;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class QESingleContainerHolder<CONTAINER> extends QEConfigHolder<@Nullable CONTAINER> implements ISingleContainerHolder<CONTAINER> {

    public QESingleContainerHolder(TileEntityQuantumEntangloporter entangloporter, TransmissionType transmissionType, Function<ISlotInfo, @Nullable CONTAINER> slotInfoParser,
          Function<InventoryFrequency, CONTAINER> containerResolver) {
        super(entangloporter, transmissionType, slotInfoParser, containerResolver);
    }

    @Nullable
    @Override
    public CONTAINER getContainer(@Nullable Direction side) {
        return getData(side);
    }

    @Nullable
    @Override
    protected CONTAINER defaultValue() {
        return null;
    }
}