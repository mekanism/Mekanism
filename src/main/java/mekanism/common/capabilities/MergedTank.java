package mekanism.common.capabilities;

import java.util.Arrays;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.FluidTankWrapper;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MergedTank extends MergedChemicalTank {

    public static MergedTank create(IExtendedFluidTank fluidTank, IGasTank gasTank, IInfusionTank infusionTank, IPigmentTank pigmentTank) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
        return new MergedTank(fluidTank, gasTank, infusionTank, pigmentTank);
    }

    private final IExtendedFluidTank fluidTank;

    private MergedTank(IExtendedFluidTank fluidTank, IChemicalTank<?, ?>... chemicalTanks) {
        super(fluidTank::isEmpty, chemicalTanks);
        this.fluidTank = new FluidTankWrapper(fluidTank, () -> Arrays.stream(chemicalTanks).allMatch(IChemicalTank::isEmpty));
    }

    public IExtendedFluidTank getFluidTank() {
        return fluidTank;
    }
}