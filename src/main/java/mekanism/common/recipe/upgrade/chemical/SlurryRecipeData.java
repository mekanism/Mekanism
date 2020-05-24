package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.slurry.BasicSlurryTank;
import mekanism.api.chemical.slurry.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryHandlerWrapper;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlurryRecipeData extends ChemicalRecipeData<ISlurryHandler, Slurry, SlurryStack, ISlurryTank> {

    public SlurryRecipeData(ListNBT tanks) {
        super(tanks);
    }

    private SlurryRecipeData(List<ISlurryTank> tanks) {
        super(tanks);
    }

    @Override
    protected SlurryRecipeData create(List<ISlurryTank> tanks) {
        return new SlurryRecipeData(tanks);
    }

    @Override
    protected SubstanceType getSubstanceType() {
        return SubstanceType.SLURRY;
    }

    @Override
    protected ISlurryTank createTank() {
        return BasicSlurryTank.create(Long.MAX_VALUE, null);
    }

    @Override
    protected ISlurryTank createTank(long capacity, Predicate<@NonNull Slurry> validator) {
        return BasicSlurryTank.create(capacity, validator, null);
    }

    @Override
    protected IChemicalHandlerWrapper<Slurry, SlurryStack> wrap(ISlurryHandler handler) {
        return new SlurryHandlerWrapper(handler);
    }

    @Override
    protected ISlurryHandler getOutputHandler(List<ISlurryTank> tanks) {
        return new IMekanismSlurryHandler() {
            @Nonnull
            @Override
            public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
                return tanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
    }

    @Override
    protected Capability<ISlurryHandler> getCapability() {
        return Capabilities.SLURRY_HANDLER_CAPABILITY;
    }

    @Override
    protected Predicate<Slurry> cloneValidator(ISlurryHandler handler, int tank) {
        return type -> handler.isSlurryValid(tank, new SlurryStack(type, 1));
    }
}