package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasRecipeData extends ChemicalRecipeData<Gas, GasStack, IGasTank, IGasHandler> {

    public GasRecipeData(ListNBT tanks) {
        super(tanks);
    }

    private GasRecipeData(List<IGasTank> tanks) {
        super(tanks);
    }

    @Override
    protected GasRecipeData create(List<IGasTank> tanks) {
        return new GasRecipeData(tanks);
    }

    @Override
    protected SubstanceType getSubstanceType() {
        return SubstanceType.GAS;
    }

    @Override
    protected IGasTank createTank() {
        return BasicGasTank.create(Long.MAX_VALUE, null);
    }

    @Override
    protected IGasTank createTank(long capacity, Predicate<@NonNull Gas> validator) {
        return BasicGasTank.create(capacity, validator, null);
    }

    @Override
    protected IGasHandler getOutputHandler(List<IGasTank> tanks) {
        return new IMekanismGasHandler() {
            @Nonnull
            @Override
            public List<IGasTank> getChemicalTanks(@Nullable Direction side) {
                return tanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
    }

    @Override
    protected Capability<IGasHandler> getCapability() {
        return Capabilities.GAS_HANDLER_CAPABILITY;
    }

    @Override
    protected Predicate<Gas> cloneValidator(IGasHandler handler, int tank) {
        return type -> handler.isValid(tank, new GasStack(type, 1));
    }
}