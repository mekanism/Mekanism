package mekanism.common.capabilities.holder.energy;

import java.util.function.Function;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class EnergyConfigHolder extends ConfigHolder<@Nullable IEnergyContainer> implements IEnergyContainerHolder {

    public static final Function<ISlotInfo, @Nullable IEnergyContainer> SLOT_PARSER = slotInfo -> slotInfo instanceof EnergySlotInfo info ? info.getContainer() : null;

    private final IEnergyContainer container;

    public EnergyConfigHolder(IEnergyContainer container, ISideConfiguration sideConfiguration) {
        super(sideConfiguration, TransmissionType.ENERGY, SLOT_PARSER);
        this.container = container;
    }

    @Nullable
    @Override
    public IEnergyContainer getContainer(@Nullable Direction side) {
        return getData(side);
    }

    @Override
    protected @Nullable IEnergyContainer defaultValue() {
        return null;
    }

    @Override
    protected @Nullable IEnergyContainer allData() {
        return container;
    }
}