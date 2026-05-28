package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.attachments.containers.type.ContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyRecipeData implements RecipeUpgradeData<EnergyRecipeData> {

    private final List<IEnergyContainer> energyContainers;

    EnergyRecipeData(List<IEnergyContainer> energyContainers) {
        this.energyContainers = energyContainers;
    }

    @Nullable
    @Override
    public EnergyRecipeData merge(EnergyRecipeData other) {
        List<IEnergyContainer> allContainers = new ArrayList<>(energyContainers);
        allContainers.addAll(other.energyContainers);
        return new EnergyRecipeData(allContainers);
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess) {
        if (energyContainers.isEmpty()) {
            return true;
        }
        IStrictEnergyHandler outputHandler = ContainerType.ENERGY.getCapOrUnexposed(itemAccess);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            for (IEnergyContainer energyContainer : this.energyContainers) {
                if (!energyContainer.isEmpty()) {
                    long toInsert = energyContainer.energy();
                    //Insert into the output using manual as the automation type
                    if (insertInto(outputHandler, toInsert, transaction) < toInsert) {
                        //If we have a remainder, stop trying to insert as our upgraded item's buffer is just full
                        //Note: We don't fail, as we allow voiding excess energy for upgrade recipes
                        break;
                    }
                }
            }
            transaction.commit();
            return true;
        }
    }

    private long insertInto(IStrictEnergyHandler handler, final long amount, TransactionContext transaction) {
        if (handler instanceof IMekanismStrictEnergyHandler mekHandler) {
            return insertInto(mekHandler.getContainers(), amount, transaction);
        } else if (amount > Integer.MAX_VALUE) {
            //We don't know how to force insert into non mekanism handlers, so if we end up with trying to, just return that we can't
            return 0;
        }
        //Note: As in general we should never reach this branch, just insert and don't bother stacking, it isn't worth adding a helper to try and insert it stacking
        return handler.insert(amount, transaction);
    }

    private long insertInto(List<IEnergyContainer> containers, final long amount, TransactionContext transaction) {
        if (containers.isEmpty()) {
            return 0;
        } else if (containers.size() == 1) {
            return insertInto(containers.getFirst(), amount, transaction);
        }
        long inserted = 0;
        List<IEnergyContainer> emptyContainers = new ArrayList<>();
        for (IEnergyContainer container : containers) {
            if (container.isEmpty()) {
                //If the container is empty, add it to a list of containers that we will check afterward
                emptyContainers.add(container);
            } else {
                inserted += insertInto(container, amount - inserted, transaction);
                if (inserted == amount) {
                    break;
                }
            }
        }
        for (IEnergyContainer container : emptyContainers) {
            inserted += insertInto(container, amount - inserted, transaction);
            if (inserted == amount) {
                return inserted;
            }
        }
        return inserted;
    }

    /**
     * Similar to {@link IEnergyContainer#insert(long, TransactionContext, AutomationType)} except directly sets the contents ignoring any rate limits, and
     * supporting if the amount is greater than max long.
     */
    private long insertInto(IEnergyContainer container, final long amount, TransactionContext transaction) {
        long capacity = container.capacity();
        long stored = container.energy();
        long needed = capacity - stored;
        if (needed > 0 && container.isValidForInsertion(AutomationType.MANUAL)) {
            long toAdd = Math.min(needed, amount);
            if (toAdd > 0) {
                container.setEnergy(stored + toAdd, transaction);
                return toAdd;
            }
        }
        return 0;
    }
}