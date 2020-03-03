package mekanism.common.tile;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityBoilerValve extends TileEntityBoilerCasing {

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
                                structure.steamTank.extract(handler.fill(fluid, FluidAction.EXECUTE), Action.EXECUTE, AutomationType.INTERNAL);
                            }
                        });
                    }
                });
            }
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
        //TODO: Re-evaluate this (Though in reality we are not bothering as the steam tank is going to be changed
        // into a gas tank so we won't have to actually evaluate the positional requirements)
        if (side == null) {
            return Arrays.asList(structure.steamTank, structure.waterTank);
        }
        if (structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1) {
            return Collections.singletonList(structure.steamTank);
        }
        return Collections.singletonList(structure.waterTank);
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.waterTank.getFluidAmount(), structure.waterTank.getCapacity());
    }
}