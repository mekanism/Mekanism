package mekanism.generators.common.tile.fusion;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigurable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;

public class TileEntityFusionReactorPort extends TileEntityFusionReactorBlock implements IConfigurable {

    public TileEntityFusionReactorPort() {
        super(GeneratorsBlocks.FUSION_REACTOR_PORT);
        delaySupplier = () -> 0;
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        //Note: We can just use a proxied holder as the input/output restrictions are done in the tanks themselves
        return side -> getMultiblock().getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        return side -> getMultiblock().getEnergyContainers(side);
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(CachedAmbientTemperature ambientTemperature) {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        return side -> getMultiblock().getInventorySlots(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        if (type == SubstanceType.GAS || type == SubstanceType.FLUID || type == SubstanceType.ENERGY || type == SubstanceType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public boolean persistInventory() {
        return false;
    }

    @Override
    protected boolean onUpdateServer(FusionReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        if (getActive() && multiblock.isFormed()) {
            Set<Direction> directionsToEmit = multiblock.getDirectionsToEmit(getBlockPos());
            ChemicalUtil.emit(directionsToEmit, multiblock.steamTank, this);
            CableUtils.emit(directionsToEmit, multiblock.energyContainer, this);
        }
        return needsPacket;
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@Nonnull Direction side) {
        if (canHandleHeat() && getHeatCapacitorCount(side) > 0) {
            TileEntity adj = WorldUtils.getTileEntity(getLevel(), getBlockPos().relative(side));
            if (!(adj instanceof TileEntityFusionReactorBlock)) {
                return CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite()).resolve().orElse(null);
            }
        }
        return null;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendMessage(MekanismUtils.logFormat(GeneratorsLang.REACTOR_PORT_EJECT.translate(InputOutput.of(oldMode, true))), Util.NIL_UUID);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }

    //Methods relating to IComputerTile
    @Override
    public boolean exposesMultiblockToComputer() {
        return false;
    }

    @ComputerMethod
    private boolean getMode() {
        return getActive();
    }

    @ComputerMethod
    private void setMode(boolean output) {
        setActive(output);
    }
    //End methods IComputerTile
}