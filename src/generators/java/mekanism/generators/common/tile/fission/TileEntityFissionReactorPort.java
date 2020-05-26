package mekanism.generators.common.tile.fission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode.FissionPortMode;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityFissionReactorPort extends TileEntityFissionReactorCasing implements IConfigurable {

    public TileEntityFissionReactorPort() {
        super(GeneratorsBlocks.FISSION_REACTOR_PORT);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (getMultiblock().isFormed()) {
            FissionPortMode mode = getMode();

            if (mode == FissionPortMode.OUTPUT_COOLANT) {
                GasUtils.emit(getMultiblock().getDirectionsToEmit(getPos()), getMultiblock().heatedCoolantTank, this);
            } else if (mode == FissionPortMode.OUTPUT_WASTE) {
                GasUtils.emit(getMultiblock().getDirectionsToEmit(getPos()), getMultiblock().wasteTank, this);
            }
        }
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        IHeatHandler handler = super.getAdjacent(side);
        if (handler != null) {
            if (MekanismUtils.getTileEntity(getWorld(), getPos().offset(side)) instanceof TileEntityFissionReactorPort) {
                return null;
            }
        }
        return handler;
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(@Nonnull IMekanismGasHandler handler) {
        return side -> getMultiblock().getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        if (type == SubstanceType.HEAT || type == SubstanceType.GAS || type == SubstanceType.FLUID) {
            return false;
        }
        return super.persists(type);
    }

    private FissionPortMode getMode() {
        return getBlockState().get(AttributeStateFissionPortMode.modeProperty);
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            FissionPortMode mode = getMode();
            mode = FissionPortMode.values()[(mode.ordinal() + 1) % FissionPortMode.values().length];
            world.setBlockState(pos, getBlockState().with(AttributeStateFissionPortMode.modeProperty, mode));
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  MekanismLang.BOILER_VALVE_MODE_CHANGE.translateColored(EnumColor.GRAY, mode.translate())));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, Direction side, Action action) {
        FluidStack ret = super.insertFluid(stack, side, action);
        if (ret.getAmount() < stack.getAmount() && action.execute()) {
            triggerValveTransfer();
        }
        return ret;
    }

    @Override
    public boolean insertGasCheck(int tank, @Nullable Direction side) {
        if (getMode() != FissionPortMode.INPUT) {
            //Don't allow inserting into the fuel tanks, if we are on output mode
            return false;
        }
        return super.insertGasCheck(tank, side);
    }

    @Override
    public boolean extractGasCheck(int tank, @Nullable Direction side) {
        //TODO: Do this better so there is no magic numbers
        FissionPortMode mode = getMode();
        if (mode == FissionPortMode.INPUT || (tank == 2 && mode == FissionPortMode.OUTPUT_COOLANT) || (tank == 1 && mode == FissionPortMode.OUTPUT_WASTE)) {
            // don't allow extraction from tanks based on mode
            return false;
        }
        return super.extractGasCheck(tank, side);
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}
