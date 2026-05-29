package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismEnergyHandler;
import mekanism.common.attachments.containers.type.ContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
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
        EnergyHandler outputHandler = ContainerType.ENERGY.getCapOrUnexposed(itemAccess);
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

    private long insertInto(EnergyHandler handler, final long amount, TransactionContext transaction) {
        if (handler instanceof IMekanismEnergyHandler mekHandler) {
            IEnergyContainer container = mekHandler.getEnergyContainer();
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
        } else if (amount > Integer.MAX_VALUE) {
            //We don't know how to force insert into non mekanism handlers, so if we end up with trying to, just return that we can't
            return 0;
        }
        return handler.insert((int) amount, transaction);
    }
}