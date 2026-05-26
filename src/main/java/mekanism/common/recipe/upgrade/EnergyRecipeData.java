package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.util.EnergyUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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
                    if (EnergyUtils.insertManual(outputHandler, toInsert, transaction) < toInsert) {
                        //If we have a remainder, stop trying to insert as our upgraded item's buffer is just full
                        break;
                    }
                }
            }
            transaction.commit();
            return true;
        }
    }
}