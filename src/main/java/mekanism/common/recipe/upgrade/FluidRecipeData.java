package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.attachments.containers.ComponentBackedResourceHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidRecipeData implements RecipeUpgradeData<FluidRecipeData> {

    private final List<IFluidTank> fluidTanks;

    FluidRecipeData(List<IFluidTank> fluidTanks) {
        this.fluidTanks = fluidTanks;
    }

    @Nullable
    @Override
    public FluidRecipeData merge(FluidRecipeData other) {
        List<IFluidTank> allTanks = new ArrayList<>(fluidTanks);
        allTanks.addAll(other.fluidTanks);
        return new FluidRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess) {
        if (fluidTanks.isEmpty()) {
            return true;
        }
        ComponentBackedResourceHandler<FluidResource, IFluidTank> outputHandler = ContainerType.FLUID.createHandler(itemAccess);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            for (IFluidTank fluidTank : this.fluidTanks) {
                if (!fluidTank.isEmpty()) {
                    FluidResource fluidType = fluidTank.resource();
                    int toInsert = fluidTank.amountAsInt();
                    //Insert into the output using manual as the automation type
                    if (outputHandler.insert(fluidType, toInsert, transaction, AutomationType.MANUAL) < toInsert) {
                        //If we have a remainder something failed so bail
                        return false;
                    }
                }
            }
            transaction.commit();
            return true;
        }
    }
}