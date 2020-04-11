package mekanism.generators.common.tile.reactor;

import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.ProxiedEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
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
public class TileEntityReactorPort extends TileEntityReactorBlock implements IConfigurable {

    public TileEntityReactorPort() {
        super(GeneratorsBlocks.REACTOR_PORT);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        //Note: We don't use a ProxiedGasTankHolder, as we need to check tank index as well for our limiting insert/output
        return side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getFluidTanks(side);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        return ProxiedEnergyContainerHolder.create(side -> true, side -> getActive(),
              side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getEnergyContainers(side));
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getHeatCapacitors(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        if (type == SubstanceType.GAS || type == SubstanceType.FLUID || type == SubstanceType.ENERGY || type == SubstanceType.HEAT) {
            return false;
        }
        return super.persists(type);
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
        if (getActive() && getReactor() != null) {
            GasUtils.emit(getReactor().getSteamTank(), this);
            CableUtils.emit(getReactor().controller.energyContainer, this);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        if (adj instanceof TileEntityReactorBlock) {
            return null;
        }
        return MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite())).orElse(null);
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
    public GasStack extractGas(int tank, long amount, @Nullable Direction side, @Nonnull Action action) {
        //TODO: Do this better so there is no magic numbers
        if (tank == 3 && !getActive()) {
            //Don't allow extracting from the steam tank, if we are on input mode
            return GasStack.EMPTY;
        }
        return super.extractGas(tank, amount, side, action);
    }
}