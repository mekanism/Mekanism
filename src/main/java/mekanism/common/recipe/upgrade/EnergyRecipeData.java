package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.math.LongTransferUtils;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
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
    public boolean applyToStack(HolderLookup.Provider provider, ItemStack stack) {
        if (energyContainers.isEmpty()) {
            return true;
        }
        IMekanismStrictEnergyHandler outputHandler = ContainerType.ENERGY.createHandler(stack);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        for (IEnergyContainer energyContainer : this.energyContainers) {
            if (!energyContainer.isEmpty() && insertManualIntoOutputContainer(outputHandler, energyContainer.getEnergy()) > 0) {
                //If we have a remainder, stop trying to insert as our upgraded item's buffer is just full
                break;
            }
        }
        return true;
    }

    private long insertManualIntoOutputContainer(IMekanismStrictEnergyHandler outputHandler, long energy) {
        //Insert into the output using manual as the automation type
        return LongTransferUtils.insert(energy, null, outputHandler::getEnergyContainers, Action.EXECUTE, AutomationType.MANUAL);
    }
}