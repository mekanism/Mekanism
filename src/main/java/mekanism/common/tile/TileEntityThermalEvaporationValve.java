package mekanism.common.tile;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock {

    private boolean prevMaster = false;

    public TileEntityThermalEvaporationValve() {
        super(MekanismBlocks.THERMAL_EVAPORATION_VALVE);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> getController() == null ? Collections.emptyList() : getController().getFluidTanks(side);
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return side -> getController() == null ? Collections.emptyList() : getController().getHeatCapacitors(side);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        return side -> getController() == null ? Collections.emptyList() : getController().getInventorySlots(side);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if ((master == null) == prevMaster) {
            for (Direction side : EnumUtils.DIRECTIONS) {
                BlockPos offset = pos.offset(side);
                if (!world.isAirBlock(offset) && MekanismUtils.getTileEntity(TileEntityThermalEvaporationBlock.class, world, offset) == null) {
                    MekanismUtils.notifyNeighborofChange(world, offset, pos);
                }
            }
        }
        prevMaster = master != null;
    }

    @Override
    public void controllerGone() {
        super.controllerGone();
        invalidateCachedCapabilities();
    }

    @Override
    public boolean persists(SubstanceType type) {
        //But that we do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.FLUID || type == SubstanceType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public boolean persistInventory() {
        return false;
    }

    @Override
    public int getRedstoneLevel() {
        TileEntityThermalEvaporationController controller = getController();
        if (controller == null) {
            return 0;
        }
        return MekanismUtils.redstoneLevelFromContents(controller.inputTank.getFluidAmount(), controller.inputTank.getCapacity());
    }
}