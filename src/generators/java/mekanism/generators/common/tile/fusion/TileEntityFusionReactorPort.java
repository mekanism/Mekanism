package mekanism.generators.common.tile.fusion;

import java.util.Set;
import mekanism.api.IContentsListener;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFusionReactorPort extends TileEntityFusionReactorBlock {

    public TileEntityFusionReactorPort(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FUSION_REACTOR_PORT, pos, state);
        delaySupplier = () -> 0;
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        //Note: We can just use a proxied holder as the input/output restrictions are done in the tanks themselves
        return side -> getMultiblock().getGasTanks(side);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        return side -> getMultiblock().getEnergyContainers(side);
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
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
    public IHeatHandler getAdjacent(@NotNull Direction side) {
        if (canHandleHeat() && getHeatCapacitorCount(side) > 0) {
            BlockEntity adj = WorldUtils.getTileEntity(getLevel(), getBlockPos().relative(side));
            if (!(adj instanceof TileEntityFusionReactorBlock)) {
                return CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER, side.getOpposite()).resolve().orElse(null);
            }
        }
        return null;
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendSystemMessage(MekanismUtils.logFormat(GeneratorsLang.REACTOR_PORT_EJECT.translate(InputOutput.of(oldMode, true))));
        }
        return InteractionResult.SUCCESS;
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