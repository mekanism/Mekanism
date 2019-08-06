package mekanism.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.MekanismBlock;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.content.boiler.BoilerSteamTank;
import mekanism.common.content.boiler.BoilerTank;
import mekanism.common.content.boiler.BoilerWaterTank;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityBoilerValve extends TileEntityBoilerCasing implements IFluidHandlerWrapper, IComputerIntegration, IComparatorSupport {

    private static final String[] methods = new String[]{"isFormed", "getSteam", "getWater", "getBoilRate", "getMaxBoilRate", "getTemp"};
    public BoilerTank waterTank;
    public BoilerTank steamTank;
    private int currentRedstoneLevel;

    public TileEntityBoilerValve() {
        super(MekanismBlock.BOILER_VALVE);
        waterTank = new BoilerWaterTank(this);
        steamTank = new BoilerSteamTank(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            if (structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1) {
                if (structure.steamStored != null && structure.steamStored.amount > 0) {
                    EmitUtils.forEachSide(getWorld(), getPos(), EnumSet.allOf(Direction.class), (tile, side) -> {
                        if (!(tile instanceof TileEntityBoilerValve)) {
                            IFluidHandler handler = CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
                            if (handler != null && PipeUtils.canFill(handler, structure.steamStored)) {
                                structure.steamStored.amount -= handler.fill(structure.steamStored, true);
                                if (structure.steamStored.amount <= 0) {
                                    structure.steamStored = null;
                                }
                            }
                        }
                    });
                }
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
        }
    }

    @Override
    public FluidTankInfo[] getTankInfo(Direction from) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1) {
                return new FluidTankInfo[]{steamTank.getInfo()};
            }
            return new FluidTankInfo[]{waterTank.getInfo()};
        }
        return PipeUtils.EMPTY;
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return new FluidTankInfo[]{steamTank.getInfo(), waterTank.getInfo()};
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, boolean doFill) {
        return waterTank.fill(resource, doFill);
    }

    @Override
    @Nullable
    public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
        return steamTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            return structure.upperRenderLocation != null && getPos().getY() < structure.upperRenderLocation.y - 1 && fluid.getFluid() == FluidRegistry.WATER;
        }
        return false;
    }

    @Override
    public boolean canDrain(Direction from, @Nullable FluidStack fluid) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            return structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1 && FluidContainerUtils.canDrain(structure.steamStored, fluid);
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
                    return new Object[]{structure.steamStored != null ? structure.steamStored.amount : 0};
                case 2:
                    return new Object[]{structure.waterStored != null ? structure.waterStored.amount : 0};
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

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return true;
            }
        }
        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(waterTank.getFluidAmount(), waterTank.getCapacity());
    }
}