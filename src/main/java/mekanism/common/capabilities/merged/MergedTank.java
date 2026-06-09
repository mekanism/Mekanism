package mekanism.common.capabilities.merged;

import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.util.NBTUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
        if (fluidTank.isEmpty()) {
            if (chemicalTank.isEmpty()) {
                return CurrentType.EMPTY;
            }
            return CurrentType.CHEMICAL;
        }
        return CurrentType.FLUID;
    }

    public IResourceContainer<?> getCurrentContainer() {
        if (chemicalTank.isEmpty()) {
            return fluidTank;
        }
        return chemicalTank;
    }

    public final IFluidTank getFluidTank() {
        return fluidTank;
    }

    public final IChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    public void addToUpdateTag(ValueOutput output) {
        NBTUtils.storeNonEmpty(output, SerializationConstants.FLUID, fluidTank);
        NBTUtils.storeNonEmpty(output, SerializationConstants.CHEMICAL, chemicalTank);
    }

    public void readFromUpdateTag(ValueInput input) {
        NBTUtils.readOrEmpty(input, SerializationConstants.FLUID, fluidTank);
        NBTUtils.readOrEmpty(input, SerializationConstants.CHEMICAL, chemicalTank);
    }

    public enum CurrentType {
        EMPTY,
        FLUID,
        CHEMICAL
    }
}