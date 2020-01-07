package mekanism.common.tile;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.content.tank.DynamicFluidTank;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TileEntityDynamicValve extends TileEntityDynamicTank implements IFluidHandlerWrapper, IComparatorSupport {

    //TODO: Move the dynamic fluid tank to SynchronizedTankData??
    // There is no real sense in having them be separate
    public DynamicFluidTank fluidTank;
    private int currentRedstoneLevel;

    public TileEntityDynamicValve() {
        super(MekanismBlocks.DYNAMIC_VALVE);
        fluidTank = new DynamicFluidTank(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            World world = getWorld();
            if (world != null) {
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
        }
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        return ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) ? new IFluidTank[]{fluidTank} : PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return fluidTank.fill(resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        return fluidTank.drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return (!isRemote() && structure != null) || (isRemote() && clientHasStructure);
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) && FluidContainerUtils.canDrain(Objects.requireNonNull(structure).fluidStored, fluid);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }
}