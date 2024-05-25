package mekanism.common.capabilities.merged;

import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public class MergedTank extends MergedChemicalTank {

    public static MergedTank create(IExtendedFluidTank fluidTank, IGasTank gasTank, IInfusionTank infusionTank, IPigmentTank pigmentTank, ISlurryTank slurryTank) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
        Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
        Objects.requireNonNull(slurryTank, "Slurry tank cannot be null");
        return new MergedTank(fluidTank, gasTank, infusionTank, pigmentTank, slurryTank);
    }

    private final IExtendedFluidTank fluidTank;

    private MergedTank(IExtendedFluidTank fluidTank, IChemicalTank<?, ?>... chemicalTanks) {
        super(fluidTank::isEmpty, chemicalTanks);
        this.fluidTank = new FluidTankWrapper(this, fluidTank, chemicalTanks);
    }

    public CurrentType getCurrentType() {
        if (!getFluidTank().isEmpty()) {
            return CurrentType.FLUID;
        }
        return switch (getCurrent()) {
            case EMPTY -> CurrentType.EMPTY;
            case GAS -> CurrentType.GAS;
            case INFUSION -> CurrentType.INFUSION;
            case PIGMENT -> CurrentType.PIGMENT;
            case SLURRY -> CurrentType.SLURRY;
        };
    }

    public final IExtendedFluidTank getFluidTank() {
        return fluidTank;
    }

    public void addToUpdateTag(HolderLookup.Provider provider, CompoundTag updateTag) {
        updateTag.put(SerializationConstants.FLUID, getFluidTank().getFluid().saveOptional(provider));
        updateTag.put(SerializationConstants.GAS, getGasTank().getStack().saveOptional(provider));
        updateTag.put(SerializationConstants.INFUSE_TYPE, getInfusionTank().getStack().saveOptional(provider));
        updateTag.put(SerializationConstants.PIGMENT, getPigmentTank().getStack().saveOptional(provider));
        updateTag.put(SerializationConstants.SLURRY, getSlurryTank().getStack().saveOptional(provider));
    }

    public void readFromUpdateTag(HolderLookup.Provider provider, CompoundTag tag) {
        NBTUtils.setFluidStackIfPresent(provider, tag, SerializationConstants.FLUID, value -> getFluidTank().setStack(value));
        NBTUtils.setGasStackIfPresent(provider, tag, SerializationConstants.GAS, value -> getGasTank().setStack(value));
        NBTUtils.setInfusionStackIfPresent(provider, tag, SerializationConstants.INFUSE_TYPE, value -> getInfusionTank().setStack(value));
        NBTUtils.setPigmentStackIfPresent(provider, tag, SerializationConstants.PIGMENT, value -> getPigmentTank().setStack(value));
        NBTUtils.setSlurryStackIfPresent(provider, tag, SerializationConstants.SLURRY, value -> getSlurryTank().setStack(value));
    }

    public enum CurrentType {
        EMPTY,
        FLUID,
        GAS,
        INFUSION,
        PIGMENT,
        SLURRY
    }
}