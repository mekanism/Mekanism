package mekanism.generators.common.tile.fission;

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
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode.FissionPortMode;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityFissionReactorPort extends TileEntityFissionReactorCasing implements IConfigurable {

    public TileEntityFissionReactorPort() {
        super(GeneratorsBlocks.FISSION_REACTOR_PORT);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null) {
            FissionPortMode mode = getMode();

            if (mode != FissionPortMode.INPUT) {
                CableUtils.emit(structure.energyContainer, this);
            }

            if (mode == FissionPortMode.OUTPUT_STEAM) {
                GasUtils.emit(structure.steamTank, this);
            } else if (mode == FissionPortMode.OUTPUT_WASTE) {
                GasUtils.emit(structure.wasteTank, this);
            }
        }
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        IHeatHandler handler = super.getAdjacent(side);
        if (super.getAdjacent(side) != null) {
            if (MekanismUtils.getTileEntity(getWorld(), getPos().offset(side)) instanceof TileEntityFissionReactorPort) {
                return null;
            }
        }
        return handler;
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.getFluidTanks(side);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        return side -> structure == null ? Collections.emptyList() : structure.getEnergyContainers(side);
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return side -> structure == null ? Collections.emptyList() : structure.getHeatCapacitors(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        if (type == SubstanceType.HEAT || type == SubstanceType.GAS || type == SubstanceType.FLUID || type == SubstanceType.ENERGY) {
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
                  GeneratorsLang.FISSION_PORT_MODE_CHANGE.translateColored(EnumColor.GRAY, mode.translate())));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Nonnull
    @Override
    public GasStack insertGas(int tank, @Nonnull GasStack stack, @Nullable Direction side, @Nonnull Action action) {
        //TODO: Do this better so there is no magic numbers
        if (getMode() != FissionPortMode.INPUT) {
            //Don't allow inserting into the fuel tanks, if we are on output mode
            return stack;
        }
        return super.insertGas(tank, stack, side, action);
    }

    @Nonnull
    @Override
    public GasStack extractGas(int tank, long amount, @Nullable Direction side, @Nonnull Action action) {
        //TODO: Do this better so there is no magic numbers
        FissionPortMode mode = getMode();
        if (mode == FissionPortMode.INPUT || (tank == 2 && mode == FissionPortMode.OUTPUT_STEAM) || (tank == 3 && mode == FissionPortMode.OUTPUT_WASTE)) {
            // don't allow extraction from tanks based on mode
            return GasStack.EMPTY;
        }
        return super.extractGas(tank, amount, side, action);
    }
}
