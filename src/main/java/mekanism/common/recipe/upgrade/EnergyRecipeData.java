package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.AttachedEnergyContainers;
import mekanism.common.attachments.containers.ContainerType;
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
    public boolean applyToStack(ItemStack stack) {
        if (energyContainers.isEmpty()) {
            return true;
        }
        AttachedEnergyContainers outputHandler = ContainerType.ENERGY.getAttachment(stack);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        for (IEnergyContainer energyContainer : this.energyContainers) {
            //TODO - 1.20.4: We probably need to do this as manual for the automation type as we don't want to be limited in our transfer rate
            if (!outputHandler.insertEnergy(energyContainer.getEnergy(), Action.EXECUTE).isZero()) {
                //If we have a remainder, stop trying to insert as our upgraded item's buffer is just full
                break;
            }
        }
        return true;
    }
}