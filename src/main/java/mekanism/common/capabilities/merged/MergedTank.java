package mekanism.common.capabilities.merged;

import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class MergedTank {

    public static MergedTank create(IExtendedFluidTank fluidTank, IChemicalTank gasTank) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        return new MergedTank(fluidTank, gasTank);
    }

    private final IExtendedFluidTank fluidTank;
    private final IChemicalTank chemicalTank;

    private MergedTank(IExtendedFluidTank fluidTank, IChemicalTank chemicalTank) {
        this.fluidTank = new FluidTankWrapper(this, fluidTank, chemicalTank);
        this.chemicalTank = new ChemicalTankWrapper(this, chemicalTank, this.fluidTank::isEmpty);
    }

    public CurrentType getCurrentType() {
        if (!getFluidTank().isEmpty()) {
            return CurrentType.FLUID;
        }
        return chemicalTank.isEmpty() ? CurrentType.EMPTY : CurrentType.CHEMICAL;
    }

    public final IExtendedFluidTank getFluidTank() {
        return fluidTank;
    }

    public final IChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    public void addToUpdateTag(@NotNull ValueOutput output) {
        output.store(SerializationConstants.FLUID, FluidStack.OPTIONAL_CODEC, fluidTank.getFluid());
        output.store(SerializationConstants.CHEMICAL, ChemicalStack.OPTIONAL_CODEC, chemicalTank.getStack());
    }

    public void readFromUpdateTag(@NotNull ValueInput input) {
        input.read(SerializationConstants.FLUID, FluidStack.OPTIONAL_CODEC).ifPresent(fluidTank::setStack);
        input.read(SerializationConstants.CHEMICAL, ChemicalStack.OPTIONAL_CODEC).ifPresent(chemicalTank::setStack);

    }

    public enum CurrentType {
        EMPTY,
        FLUID,
        CHEMICAL
    }
}