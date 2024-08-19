package mekanism.common.capabilities.merged;

import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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

    public void addToUpdateTag(HolderLookup.Provider provider, CompoundTag updateTag) {
        updateTag.put(SerializationConstants.FLUID, fluidTank.getFluid().saveOptional(provider));
        updateTag.put(SerializationConstants.CHEMICAL, chemicalTank.getStack().saveOptional(provider));
    }

    public void readFromUpdateTag(HolderLookup.Provider provider, CompoundTag tag) {
        NBTUtils.setFluidStackIfPresent(provider, tag, SerializationConstants.FLUID, fluidTank::setStack);
        NBTUtils.setChemicalStackIfPresent(provider, tag, SerializationConstants.CHEMICAL, chemicalTank::setStack);

    }

    public enum CurrentType {
        EMPTY,
        FLUID,
        CHEMICAL
    }
}