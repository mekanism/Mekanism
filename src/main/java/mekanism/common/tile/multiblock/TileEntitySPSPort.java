package mekanism.common.tile.multiblock;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntitySPSPort extends TileEntitySPSCasing {

    private MachineEnergyContainer<TileEntitySPSPort> energyContainer;

    public TileEntitySPSPort(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SPS_PORT, pos, state);
        delaySupplier = () -> 0;
    }

    @Override
    protected boolean onUpdateServer(SPSMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        if (multiblock.isFormed()) {
            if (getActive()) {
                ChemicalUtil.emit(multiblock.getDirectionsToEmit(getBlockPos()), multiblock.outputTank, this);
            }
            if (!energyContainer.isEmpty() && multiblock.canSupplyCoilEnergy(this)) {
                multiblock.supplyCoilEnergy(this, energyContainer.extract(energyContainer.getEnergy(), Action.EXECUTE, AutomationType.INTERNAL));
            }
        }
        return needsPacket;
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        //Note: We can just use a proxied holder as the input/output restrictions are done in the tanks themselves
        return side -> getMultiblock().getGasTanks(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        if (type == SubstanceType.GAS) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.SPS_PORT_MODE.translate(InputOutput.of(oldMode, true))));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }

    //Methods relating to IComputerTile
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