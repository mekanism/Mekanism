package mekanism.common.capabilities.proxy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;

@NothingNullByDefault
public class ProxyEnergyHandler extends ProxyHandler<@NonNull IEnergyContainerHolder> implements EnergyHandler {

    public ProxyEnergyHandler(@Nullable Direction side, IEnergyContainerHolder holder) {
        super(side, holder);
    }

    @Nullable
    private IEnergyContainer getContainer() {
        return holder.getContainer(side);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getAmountAsLong() {
        IEnergyContainer container = getContainer();
        return container == null ? 0 : container.energy();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong() {
        IEnergyContainer container = getContainer();
        return container == null ? 0 : container.capacity();
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        if (readOnlyInsert()) {
            return 0;
        }
        IEnergyContainer container = getContainer();
        return container == null ? 0 : container.insert(amount, transaction, AutomationType.handler(side));
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        if (readOnlyExtract()) {
            return 0;
        }
        IEnergyContainer container = getContainer();
        return container == null ? 0 : container.extract(amount, transaction, AutomationType.handler(side));
    }
}