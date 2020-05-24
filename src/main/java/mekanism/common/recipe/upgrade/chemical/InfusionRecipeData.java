package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionHandlerWrapper;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfusionRecipeData extends ChemicalRecipeData<IInfusionHandler, InfuseType, InfusionStack, IInfusionTank> {

    public InfusionRecipeData(ListNBT tanks) {
        super(tanks);
    }

    private InfusionRecipeData(List<IInfusionTank> tanks) {
        super(tanks);
    }

    @Override
    protected InfusionRecipeData create(List<IInfusionTank> tanks) {
        return new InfusionRecipeData(tanks);
    }

    @Override
    protected SubstanceType getSubstanceType() {
        return SubstanceType.INFUSION;
    }

    @Override
    protected IInfusionTank createTank() {
        return BasicInfusionTank.create(Long.MAX_VALUE, null);
    }

    @Override
    protected IInfusionTank createTank(long capacity, Predicate<@NonNull InfuseType> validator) {
        return BasicInfusionTank.create(capacity, validator, null);
    }

    @Override
    protected IChemicalHandlerWrapper<InfuseType, InfusionStack> wrap(IInfusionHandler handler) {
        return new InfusionHandlerWrapper(handler);
    }

    @Override
    protected IInfusionHandler getOutputHandler(List<IInfusionTank> tanks) {
        return new IMekanismInfusionHandler() {
            @Nonnull
            @Override
            public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
                return tanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
    }

    @Override
    protected Capability<IInfusionHandler> getCapability() {
        return Capabilities.INFUSION_HANDLER_CAPABILITY;
    }

    @Override
    protected Predicate<InfuseType> cloneValidator(IInfusionHandler handler, int tank) {
        return type -> handler.isInfusionValid(tank, new InfusionStack(type, 1));
    }
}