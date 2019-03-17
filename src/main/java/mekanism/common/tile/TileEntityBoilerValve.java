package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.content.boiler.BoilerSteamTank;
import mekanism.common.content.boiler.BoilerTank;
import mekanism.common.content.boiler.BoilerWaterTank;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityBoilerValve extends TileEntityBoilerCasing implements IFluidHandlerWrapper,
      IComputerIntegration {

    private static final String[] methods = new String[]{"isFormed", "getSteam", "getWater", "getBoilRate",
          "getMaxBoilRate", "getTemp"};
    public BoilerTank waterTank;
    public BoilerTank steamTank;

    public TileEntityBoilerValve() {
        super("BoilerValve");

        waterTank = new BoilerWaterTank(this);
        steamTank = new BoilerSteamTank(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            if (structure != null && structure.upperRenderLocation != null
                  && getPos().getY() >= structure.upperRenderLocation.y - 1) {
                if (structure.steamStored != null && structure.steamStored.amount > 0) {
                    for (EnumFacing side : EnumFacing.values()) {
                        TileEntity tile = Coord4D.get(this).offset(side).getTileEntity(world);

                        if (tile != null && !(tile instanceof TileEntityBoilerValve) && CapabilityUtils
                              .hasCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                                    side.getOpposite())) {
                            IFluidHandler handler = CapabilityUtils
                                  .getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                                        side.getOpposite());

                            if (PipeUtils.canFill(handler, structure.steamStored)) {
                                structure.steamStored.amount -= handler.fill(structure.steamStored, true);

                                if (structure.steamStored.amount <= 0) {
                                    structure.steamStored = null;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1) {
                return new FluidTankInfo[]{steamTank.getInfo()};
            } else {
                return new FluidTankInfo[]{waterTank.getInfo()};
            }
        }

        return PipeUtils.EMPTY;
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return new FluidTankInfo[]{steamTank.getInfo(), waterTank.getInfo()};
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (structure != null && structure.upperRenderLocation != null
              && getPos().getY() < structure.upperRenderLocation.y - 1) {
            return waterTank.fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        if (structure != null && structure.upperRenderLocation != null
              && getPos().getY() >= structure.upperRenderLocation.y - 1) {
            if (structure.steamStored != null) {
                if (resource != null && resource.getFluid() == structure.steamStored.getFluid()) {
                    return steamTank.drain(resource.amount, doDrain);
                }
            }
        }

        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        if (structure != null && structure.upperRenderLocation != null
              && getPos().getY() >= structure.upperRenderLocation.y - 1) {
            return steamTank.drain(maxDrain, doDrain);
        }

        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, @Nullable FluidStack fluid) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            return structure.upperRenderLocation != null && getPos().getY() < structure.upperRenderLocation.y - 1;
        }

        return false;
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            return structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1;
        }

        return false;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
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
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return true;
            }
        }

        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return (T) new FluidHandlerWrapper(this, side);
            }
        }

        return super.getCapability(capability, side);
    }
}
