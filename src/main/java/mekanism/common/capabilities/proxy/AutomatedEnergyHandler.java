package mekanism.common.capabilities.proxy;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class AutomatedEnergyHandler implements EnergyHandler {

    @Nullable
    public static EnergyHandler manual(@Nullable EnergyHandler handler) {
        return wrap(handler, AutomationType.MANUAL);
    }

    @Nullable
    public static EnergyHandler wrap(@Nullable EnergyHandler handler, AutomationType automationType) {
        if (handler instanceof IEnergyContainer container) {
            return new AutomatedEnergyHandler(container, null, automationType);
        } else if (handler instanceof ComponentBackedEnergyHandler energyHandler) {
            return new AutomatedEnergyHandler(null, energyHandler, automationType);
        }
        return handler;
    }

    @Nullable
    private final ComponentBackedEnergyHandler energyHandler;
    @Nullable
    private final IEnergyContainer energyContainer;
    private final EnergyHandler handler;
    private final AutomationType automationType;

    private AutomatedEnergyHandler(@Nullable IEnergyContainer energyContainer, @Nullable ComponentBackedEnergyHandler energyHandler, AutomationType automationType) {
        this.energyContainer = energyContainer;
        this.energyHandler = energyHandler;
        this.handler = Objects.requireNonNull(energyContainer == null ? energyHandler : energyContainer);
        this.automationType = automationType;
    }

    @Override
    public long getAmountAsLong() {
        return handler.getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {
        return handler.getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (energyHandler != null) {
            return energyHandler.insert(amount, transaction, automationType);
        } else if (energyContainer != null) {
            return energyContainer.insert(amount, transaction, automationType);
        }
        //Something went wrong
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (energyHandler != null) {
            return energyHandler.extract(amount, transaction, automationType);
        } else if (energyContainer != null) {
            return energyContainer.extract(amount, transaction, automationType);
        }
        //Something went wrong
        return 0;
    }
}