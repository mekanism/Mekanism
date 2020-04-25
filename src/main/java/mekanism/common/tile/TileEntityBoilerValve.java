package mekanism.common.tile;

import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode.BoilerValveMode;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityBoilerValve extends TileEntityBoilerCasing {

    public TileEntityBoilerValve() {
        super(MekanismBlocks.BOILER_VALVE);
        //If we are a valve for water, then disable the gas handler capability
        addSemiDisabledCapability(Capabilities.GAS_HANDLER_CAPABILITY,
              () -> structure != null && (structure.upperRenderLocation == null || getPos().getY() < structure.upperRenderLocation.y - 1));
        //If we are a valve for steam, then disable the fluid handler capability
        addSemiDisabledCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
              () -> structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.getFluidTanks(side);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1) {
            BoilerValveMode mode = getMode();

            if (mode == BoilerValveMode.OUTPUT_STEAM) {
                GasUtils.emit(structure.steamTank, this);
            } else if (mode == BoilerValveMode.OUTPUT_COOLANT) {
                GasUtils.emit(structure.cooledCoolantTank, this);
            }
        }
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
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.waterTank.getFluidAmount(), structure.waterTank.getCapacity());
    }

    private BoilerValveMode getMode() {
        return getBlockState().get(AttributeStateBoilerValveMode.modeProperty);
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            BoilerValveMode mode = getMode();
            mode = BoilerValveMode.values()[(mode.ordinal() + 1) % BoilerValveMode.values().length];
            world.setBlockState(pos, getBlockState().with(AttributeStateBoilerValveMode.modeProperty, mode));
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  MekanismLang.BOILER_VALVE_MODE_CHANGE.translateColored(EnumColor.GRAY, mode.translate())));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, Direction side, Action action) {
        FluidStack ret = super.insertFluid(stack, side, action);
        if (ret.getAmount() < stack.getAmount() && action.execute()) {
            if (structure != null) {
                Coord4D coord4D = Coord4D.get(this);
                for (ValveData data : structure.valves) {
                    if (coord4D.equals(data.location)) {
                        data.onTransfer();
                    }
                }
            }
        }
        return ret;
    }

    @Nonnull
    @Override
    public GasStack insertGas(int tank, @Nonnull GasStack stack, @Nullable Direction side, @Nonnull Action action) {
        //TODO: Do this better so there is no magic numbers
        if (getMode() != BoilerValveMode.INPUT) {
            //Don't allow inserting into the fuel tanks, if we are on output mode
            return stack;
        }
        return super.insertGas(tank, stack, side, action);
    }

    @Nonnull
    @Override
    public GasStack extractGas(int tank, long amount, @Nullable Direction side, @Nonnull Action action) {
        //TODO: Do this better so there is no magic numbers
        BoilerValveMode mode = getMode();
        if (mode == BoilerValveMode.INPUT || (tank == 2 && mode == BoilerValveMode.OUTPUT_STEAM) || (tank == 0 && mode == BoilerValveMode.OUTPUT_COOLANT)) {
            // don't allow extraction from tanks based on mode
            return GasStack.EMPTY;
        }
        return super.extractGas(tank, amount, side, action);
    }
}