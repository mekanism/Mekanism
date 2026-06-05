package mekanism.common.recipe.upgrade;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.util.EnergyUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyRecipeData implements RecipeUpgradeData<EnergyRecipeData> {

    private final long storedEnergy;

    EnergyRecipeData(long storedEnergy) {
        this.storedEnergy = storedEnergy;
    }

    @Nullable
    @Override
    public EnergyRecipeData merge(EnergyRecipeData other) {
        return new EnergyRecipeData(MathUtils.addClamped(this.storedEnergy, other.storedEnergy));
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess, TransactionContext transaction) {
        if (storedEnergy == 0) {
            //TODO: Do we care to support cases where the output item might have a different default component so then a value of zero for stored should be written?
            return true;
        }
        EnergyHandler handler = ContainerType.ENERGY.getCapOrUnexposed(itemAccess);
        if (handler == null) {
            //Something went wrong, fail
            return false;
        }
        //TODO - 26.1: Do we want to just directly set the component onto the stack? Also what about resistive heater usage?
        //Insert into the output using manual as the automation type
        //Note: We don't fail, as we allow voiding excess energy for upgrade recipes
        IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(handler);
        if (energyContainer != null) {
            long capacity = energyContainer.getCapacityAsLong();
            long stored = energyContainer.getAmountAsLong();
            if (energyContainer.isValidForInsertion(AutomationType.MANUAL)) {
                long toAdd = Math.min(capacity - stored, storedEnergy);
                if (toAdd > 0) {
                    energyContainer.setEnergy(stored + toAdd, transaction);
                }
            }
        }
        return true;
    }
}