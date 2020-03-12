package mekanism.generators.common.tile.reactor;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IHeatTransfer;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

//TODO: Allow reactor controller inventory slot to be interacted with via the port again
public class TileEntityReactorPort extends TileEntityReactorBlock implements IHeatTransfer, IConfigurable {

    public TileEntityReactorPort() {
        super(GeneratorsBlocks.REACTOR_PORT);
    }

    @Override
    public boolean canHandleGas() {
        //Mark that we can handle gas
        return true;
    }

    @Override
    public boolean persistGas() {
        //But that we do not handle gas when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return canHandleGas() && getReactor() != null ? getReactor().controller.getGasTanks(side) : Collections.emptyList();
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
        if (!canHandleFluid() || getReactor() == null) {
            return Collections.emptyList();
        }
        return getReactor().controller.getFluidTanks(side);
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    protected void resetChanged() {
        if (changed) {
            World world = getWorld();
            if (world != null) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
        }
        super.resetChanged();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        CableUtils.emit(this);
        if (getActive() && getReactor() != null && !getReactor().getSteamTank().isEmpty()) {
            GasUtils.emitGas(this, getReactor().getSteamTank());
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        } else if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Override
    public double getEnergy() {
        return getReactor() == null ? 0 : getReactor().getBufferedEnergy();
    }

    @Override
    public void setEnergy(double energy) {
        if (getReactor() != null) {
            getReactor().setBufferedEnergy(energy);
        }
    }

    @Override
    public double getMaxEnergy() {
        return getReactor() == null ? 0 : getReactor().getBufferSize();
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return true;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }

    @Override
    public double getMaxOutput() {
        return Integer.MAX_VALUE;
    }

    @Override
    public double getTemp() {
        return getReactor() == null ? 0 : getReactor().getTemp();
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 5;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return getReactor() == null ? 0 : getReactor().getInsulationCoefficient(side);
    }

    @Override
    public void transferHeatTo(double heat) {
        if (getReactor() != null) {
            getReactor().transferHeatTo(heat);
        }
    }

    @Override
    public double[] simulateHeat() {
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        return getReactor() == null ? 0 : getReactor().applyTemperatureChange();
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        if (adj instanceof TileEntityReactorBlock) {
            return null;
        }
        return MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())).orElse(null);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY && getReactor() == null) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  GeneratorsLang.REACTOR_PORT_EJECT.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true))));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public GasStack insertGas(int tank, @Nonnull GasStack stack, @Nullable Direction side, @Nonnull Action action) {
        //TODO: Do this better so there is no magic numbers
        if (tank < 3 && getActive()) {
            //Don't allow inserting into the fuel tanks, if we are on output mode
            return stack;
        }
        return super.insertGas(tank, stack, side, action);
    }

    @Nonnull
    @Override
    public GasStack extractGas(int tank, int amount, @Nullable Direction side, @Nonnull Action action) {
        //TODO: Do this better so there is no magic numbers
        if (tank == 3 && !getActive()) {
            //Don't allow extracting from the steam tank, if we are on input mode
            return GasStack.EMPTY;
        }
        return super.extractGas(tank, amount, side, action);
    }
}