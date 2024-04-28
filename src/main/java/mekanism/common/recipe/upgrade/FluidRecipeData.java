package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.ExtendedFluidHandlerUtils;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidRecipeData implements RecipeUpgradeData<FluidRecipeData> {

    private final List<IExtendedFluidTank> fluidTanks;

    FluidRecipeData(List<IExtendedFluidTank> fluidTanks) {
        this.fluidTanks = fluidTanks;
    }

    @Nullable
    @Override
    public FluidRecipeData merge(FluidRecipeData other) {
        List<IExtendedFluidTank> allTanks = new ArrayList<>(fluidTanks);
        allTanks.addAll(other.fluidTanks);
        return new FluidRecipeData(allTanks);
    }

    @Override
    public boolean applyToStack(HolderLookup.Provider provider, ItemStack stack) {
        if (fluidTanks.isEmpty()) {
            return true;
        }
        //TODO: Improve the logic used so that it tries to batch similar types of fluids together first
        // and maybe make it try multiple slot combinations??
        IMekanismFluidHandler outputHandler = ContainerType.FLUID.createHandler(stack);
        if (outputHandler == null) {
            //Something went wrong, fail
            return false;
        }
        for (IExtendedFluidTank fluidTank : this.fluidTanks) {
            if (!fluidTank.isEmpty() && !insertManualIntoOutputContainer(outputHandler, fluidTank.getFluid()).isEmpty()) {
                //If we have a remainder something failed so bail
                return false;
            }
        }
        return true;
    }

    private FluidStack insertManualIntoOutputContainer(IMekanismFluidHandler outputHandler, FluidStack fluid) {
        //Insert into the output using manual as the automation type
        return ExtendedFluidHandlerUtils.insert(fluid, null, outputHandler::getFluidTanks, Action.EXECUTE, AutomationType.MANUAL);
    }
}