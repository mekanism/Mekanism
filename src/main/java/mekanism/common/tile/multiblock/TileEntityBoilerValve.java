package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode.BoilerValveMode;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityBoilerValve extends TileEntityBoilerCasing {

    public TileEntityBoilerValve(BlockPos pos, BlockState state) {
        super(MekanismBlocks.BOILER_VALVE, pos, state);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        return side -> getMultiblock().getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @Override
    protected boolean onUpdateServer(BoilerMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        if (multiblock.isFormed()) {
            BoilerValveMode mode = getMode();
            if (mode == BoilerValveMode.OUTPUT_STEAM) {
                ChemicalUtil.emit(multiblock.getDirectionsToEmit(getBlockPos()), multiblock.steamTank, this);
            } else if (mode == BoilerValveMode.OUTPUT_COOLANT) {
                ChemicalUtil.emit(multiblock.getDirectionsToEmit(getBlockPos()), multiblock.cooledCoolantTank, this);
            }
        }
        return needsPacket;
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle fluid or gas when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.FLUID || type == SubstanceType.GAS) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }

    @ComputerMethod
    private BoilerValveMode getMode() {
        return getBlockState().getValue(AttributeStateBoilerValveMode.modeProperty);
    }

    @ComputerMethod
    private void setMode(BoilerValveMode mode) {
        if (mode != getMode()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AttributeStateBoilerValveMode.modeProperty, mode));
        }
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            BoilerValveMode mode = getMode().getNext();
            setMode(mode);
            player.sendMessage(MekanismUtils.logFormat(MekanismLang.BOILER_VALVE_MODE_CHANGE.translate(mode)), Util.NIL_UUID);
        }
        return InteractionResult.SUCCESS;
    }

    @Nonnull
    @Override
    public FluidStack insertFluid(@Nonnull FluidStack stack, Direction side, @Nonnull Action action) {
        FluidStack ret = super.insertFluid(stack, side, action);
        if (ret.getAmount() < stack.getAmount() && action.execute()) {
            getMultiblock().triggerValveTransfer(this);
        }
        return ret;
    }

    @Override
    public boolean insertGasCheck(int tank, @Nullable Direction side) {
        if (getMode() != BoilerValveMode.INPUT) {
            //Don't allow inserting into the fuel tanks, if we are on output mode
            return false;
        }
        return super.insertGasCheck(tank, side);
    }

    @Override
    public boolean extractGasCheck(int tank, @Nullable Direction side) {
        //TODO: Do this better so there is no magic numbers
        BoilerValveMode mode = getMode();
        if (mode == BoilerValveMode.INPUT || (tank == 2 && mode == BoilerValveMode.OUTPUT_STEAM) || (tank == 0 && mode == BoilerValveMode.OUTPUT_COOLANT)) {
            // don't allow extraction from tanks based on mode
            return false;
        }
        return super.extractGasCheck(tank, side);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private void incrementMode() {
        setMode(getMode().getNext());
    }

    @ComputerMethod
    private void decrementMode() {
        setMode(getMode().getPrevious());
    }
    //End methods IComputerTile
}