package mekanism.common.capabilities.holder;

import java.util.function.Function;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public abstract class QEConfigHolder<TYPE> extends ConfigHolder<TYPE> {

    private final Function<InventoryFrequency, TYPE> containerResolver;
    protected final TileEntityQuantumEntangloporter entangloporter;

    public QEConfigHolder(TileEntityQuantumEntangloporter entangloporter, TransmissionType transmissionType, Function<ISlotInfo, TYPE> slotInfoParser,
          Function<InventoryFrequency, TYPE> containerResolver) {
        super(entangloporter, transmissionType, slotInfoParser);
        this.entangloporter = entangloporter;
        this.containerResolver = containerResolver;
    }

    @Override
    protected TYPE allData() {
        return containerResolver.apply(entangloporter.getFreq());
    }

    @Override
    protected TYPE getData(@Nullable Direction side) {
        if (!entangloporter.hasFrequency()) {
            return defaultValue();
        }
        return super.getData(side);
    }
}