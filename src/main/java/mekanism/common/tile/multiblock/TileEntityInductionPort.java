package mekanism.common.tile.multiblock;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.ProxiedEnergyContainerHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import mekanism.common.lib.multiblock.MultiblockData.EnergyOutputTarget;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityInductionPort extends TileEntityInductionCasing {

    private final Map<Direction, BlockEnergyCapabilityCache> energyCapabilityCaches = new EnumMap<>(Direction.class);

    public TileEntityInductionPort(BlockPos pos, BlockState state) {
        super(MekanismBlocks.INDUCTION_PORT, pos, state);
        delaySupplier = NO_DELAY;
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        //Don't allow inserting if we are on output mode, or extracting if we are on input mode
        return ProxiedEnergyContainerHolder.create(side -> !getActive(), side -> getActive(), side -> getMultiblock().getEnergyContainers(side));
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //Do not handle energy when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.ENERGY) {
            return false;
        }
        return super.persists(type);
    }

    public void addEnergyTargetCapability(List<EnergyOutputTarget> outputTargets, Direction side) {
        BlockEnergyCapabilityCache cache = energyCapabilityCaches.get(side);
        if (cache == null) {
            cache = BlockEnergyCapabilityCache.create((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            energyCapabilityCaches.put(side, cache);
        }
        outputTargets.add(new EnergyOutputTarget(cache, this::getActive));
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.displayClientMessage(MekanismLang.INDUCTION_PORT_MODE.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true)), true);
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

    @ComputerMethod(methodDescription = "true -> output, false -> input")
    void setMode(boolean output) {
        setActive(output);
    }
    //End methods IComputerTile
}