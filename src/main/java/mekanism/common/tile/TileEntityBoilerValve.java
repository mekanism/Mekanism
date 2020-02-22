package mekanism.common.tile;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TileEntityBoilerValve extends TileEntityBoilerCasing implements IFluidHandlerWrapper, IComputerIntegration {

    private static final String[] methods = new String[]{"isFormed", "getSteam", "getWater", "getBoilRate", "getMaxBoilRate", "getTemp"};

    public TileEntityBoilerValve() {
        super(MekanismBlocks.BOILER_VALVE);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            if (structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1 && !structure.steamTank.isEmpty()) {
                EmitUtils.forEachSide(getWorld(), getPos(), EnumSet.allOf(Direction.class), (tile, side) -> {
                    if (!(tile instanceof TileEntityBoilerValve)) {
                        CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).ifPresent(handler -> {
                            FluidStack fluid = structure.steamTank.getFluid();
                            if (PipeUtils.canFill(handler, fluid)) {
                                structure.steamTank.setFluid(new FluidStack(fluid, fluid.getAmount() - handler.fill(fluid, FluidAction.EXECUTE)));
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) {
            if (Objects.requireNonNull(structure).upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1) {
                return new IFluidTank[]{structure.steamTank};
            }
            return new IFluidTank[]{structure.waterTank};
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return structure == null ? PipeUtils.EMPTY : new IFluidTank[]{structure.steamTank, structure.waterTank};
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        if (structure == null) {
            return 0;
        }
        return structure.waterTank.fill(resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        if (structure == null) {
            return FluidStack.EMPTY;
        }
        return structure.steamTank.drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        if ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) {
            return Objects.requireNonNull(structure).upperRenderLocation != null && getPos().getY() < structure.upperRenderLocation.y - 1 && fluid.getFluid().isIn(FluidTags.WATER);
        }
        return false;
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        if ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) {
            return Objects.requireNonNull(structure).upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1 && FluidContainerUtils.canDrain(structure.steamTank.getFluid(), fluid);
        }
        return false;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (method == 0) {
            return new Object[]{structure != null};
        } else {
            if (structure == null) {
                return new Object[]{"Unformed"};
            }
            switch (method) {
                case 1:
                    return new Object[]{structure.steamTank.getFluidAmount()};
                case 2:
                    return new Object[]{structure.waterTank.getFluidAmount()};
                case 3:
                    return new Object[]{structure.lastBoilRate};
                case 4:
                    return new Object[]{structure.lastMaxBoil};
                case 5:
                    return new Object[]{structure.temperature};
            }
        }
        throw new NoSuchMethodException();
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
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.waterTank.getFluidAmount(), structure.waterTank.getCapacity());
    }
}