package mekanism.common.capabilities.merged;

import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class MergedTank {

    public static MergedTank create(IFluidTank fluidTank, IChemicalTank gasTank) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        return new MergedTank(fluidTank, gasTank);
    }

    private final IFluidTank fluidTank;
    private final IChemicalTank chemicalTank;

    private MergedTank(IFluidTank fluidTank, IChemicalTank chemicalTank) {
        this.fluidTank = new FluidTankWrapper(this, fluidTank, chemicalTank);
        this.chemicalTank = new ChemicalTankWrapper(this, chemicalTank, this.fluidTank::isEmpty);
    }

    public CurrentType getCurrentType() {
        if (!getFluidTank().isEmpty()) {
            return CurrentType.FLUID;
        }
        return chemicalTank.isEmpty() ? CurrentType.EMPTY : CurrentType.CHEMICAL;
    }

    public final IFluidTank getFluidTank() {
        return fluidTank;
    }

    public final IChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    public void addToUpdateTag(@NotNull ValueOutput output) {
        output.store(SerializationConstants.FLUID, SerializerHelper.OPTIONAL_FLUID_RESOURCE_STACK_CODEC, fluidTank.asStack());
        output.store(SerializationConstants.CHEMICAL, SerializerHelper.OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC, chemicalTank.asStack());
    }

    public void readFromUpdateTag(@NotNull ValueInput input) {
        input.read(SerializationConstants.FLUID, SerializerHelper.OPTIONAL_FLUID_RESOURCE_STACK_CODEC).ifPresent(fluidTank::setContents);
        input.read(SerializationConstants.CHEMICAL, SerializerHelper.OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC).ifPresent(chemicalTank::setContents);

    }

    public enum CurrentType {
        EMPTY,
        FLUID,
        CHEMICAL
    }
}