package mekanism.generators.common.tile.turbine;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityTurbineVent extends TileEntityTurbineCasing {

    public TileEntityTurbineVent() {
        super(GeneratorsBlocks.TURBINE_VENT);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && !structure.ventTank.isEmpty()) {
            FluidStack fluidStack = structure.ventTank.getFluid().copy();
            EmitUtils.forEachSide(getWorld(), getPos(), EnumSet.allOf(Direction.class),
                  (tile, side) -> CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).ifPresent(handler -> {
                      if (PipeUtils.canFill(handler, fluidStack)) {
                          structure.ventTank.extract(handler.fill(fluidStack, FluidAction.EXECUTE), Action.EXECUTE, AutomationType.INTERNAL);
                      }
                  }));
        }
    }

    @Override
    public boolean canHandleFluid() {
        //Mark that we can handle fluid
        return true;
    }

    @Override
    public boolean persistFluid() {
        //But that we do not handle fluid when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        if (!canHandleFluid() || structure == null) {
            return Collections.emptyList();
        }
        return structure.ventTanks;
    }
}