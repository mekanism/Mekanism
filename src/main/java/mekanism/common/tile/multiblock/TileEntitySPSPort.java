package mekanism.common.tile.multiblock;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.MultiblockData.CapabilityOutputTarget;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntitySPSPort extends TileEntitySPSCasing {

    private final Map<Direction, BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> chemicalCapabilityCaches = new EnumMap<>(Direction.class);
    private MachineEnergyContainer<TileEntitySPSPort> energyContainer;

    public TileEntitySPSPort(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SPS_PORT, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(SPSMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        if (multiblock.isFormed()) {
            if (!energyContainer.isEmpty() && multiblock.canSupplyCoilEnergy(this)) {
                multiblock.supplyCoilEnergy(this, energyContainer.extract(energyContainer.getEnergy(), Action.EXECUTE, AutomationType.INTERNAL));
            }
        }
        return needsPacket;
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        //Note: We can just use a proxied holder as the input/output restrictions are done in the tanks themselves
        return side -> getMultiblock().getChemicalTanks(side);
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.CHEMICAL) {
            return false;
        }
        return super.persists(type);
    }

    public void addChemicalTargetCapability(List<CapabilityOutputTarget<IChemicalHandler>> outputTargets, Direction side) {
        BlockCapabilityCache<IChemicalHandler, @Nullable Direction> cache = chemicalCapabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.CHEMICAL.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            chemicalCapabilityCaches.put(side, cache);
        }
        outputTargets.add(new CapabilityOutputTarget<>(cache, this::getActive));
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.displayClientMessage(MekanismLang.SPS_PORT_MODE.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true)), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = "true -> output, false -> input.")
    boolean getMode() {// TODO change this to enum?
        return getActive();
    }

    @ComputerMethod(methodDescription = "true -> output, false -> input.")
    void setMode(boolean output) {// TODO change this to enum?
        setActive(output);
    }
    //End methods IComputerTile
}