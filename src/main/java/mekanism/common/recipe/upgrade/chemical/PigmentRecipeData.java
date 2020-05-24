package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.pigment.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentHandlerWrapper;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PigmentRecipeData extends ChemicalRecipeData<IPigmentHandler, Pigment, PigmentStack, IPigmentTank> {

    public PigmentRecipeData(ListNBT tanks) {
        super(tanks);
    }

    private PigmentRecipeData(List<IPigmentTank> tanks) {
        super(tanks);
    }

    @Override
    protected PigmentRecipeData create(List<IPigmentTank> tanks) {
        return new PigmentRecipeData(tanks);
    }

    @Override
    protected SubstanceType getSubstanceType() {
        return SubstanceType.PIGMENT;
    }

    @Override
    protected IPigmentTank createTank() {
        return BasicPigmentTank.create(Long.MAX_VALUE, null);
    }

    @Override
    protected IPigmentTank createTank(long capacity, Predicate<@NonNull Pigment> validator) {
        return BasicPigmentTank.create(capacity, validator, null);
    }

    @Override
    protected IChemicalHandlerWrapper<Pigment, PigmentStack> wrap(IPigmentHandler handler) {
        return new PigmentHandlerWrapper(handler);
    }

    @Override
    protected IPigmentHandler getOutputHandler(List<IPigmentTank> tanks) {
        return new IMekanismPigmentHandler() {
            @Nonnull
            @Override
            public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
                return tanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
    }

    @Override
    protected Capability<IPigmentHandler> getCapability() {
        return Capabilities.PIGMENT_HANDLER_CAPABILITY;
    }

    @Override
    protected Predicate<Pigment> cloneValidator(IPigmentHandler handler, int tank) {
        return type -> handler.isPigmentValid(tank, new PigmentStack(type, 1));
    }
}