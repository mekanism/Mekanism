package mekanism.common.capabilities.holder.single;

import java.util.function.Function;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class SingleConfigHolder<CONTAINER> extends ConfigHolder<@Nullable CONTAINER> implements ISingleContainerHolder<CONTAINER> {

    public static final Function<ISlotInfo, @Nullable IEnergyContainer> ENERGY_SLOT_PARSER = slotInfo -> slotInfo instanceof EnergySlotInfo info ? info.getContainer() : null;
    public static final Function<ISlotInfo, @Nullable IHeatCapacitor> HEAT_SLOT_PARSER = slotInfo -> slotInfo instanceof HeatSlotInfo info ? info.getCapacitor() : null;

    public static SingleConfigHolder<IEnergyContainer> energy(IEnergyContainer container, ISideConfiguration sideConfiguration) {
        return new SingleConfigHolder<>(container, sideConfiguration, TransmissionType.ENERGY, ENERGY_SLOT_PARSER);
    }

    public static SingleConfigHolder<IHeatCapacitor> heat(IHeatCapacitor container, ISideConfiguration sideConfiguration) {
        return new SingleConfigHolder<>(container, sideConfiguration, TransmissionType.HEAT, HEAT_SLOT_PARSER);
    }

    private final CONTAINER container;

    public SingleConfigHolder(CONTAINER container, ISideConfiguration sideConfiguration, TransmissionType transmissionType, Function<ISlotInfo, @Nullable CONTAINER> slotInfoParser) {
        super(sideConfiguration, transmissionType, slotInfoParser);
        this.container = container;
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

    @Nullable
    @Override
    protected CONTAINER allData() {
        return container;
    }
}