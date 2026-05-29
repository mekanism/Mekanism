package mekanism.common.tile.multiblock;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.IContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.ProxiedEnergyContainerHolder;
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
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.Nullable;

public class TileEntityInductionPort extends TileEntityInductionCasing {

    private final Map<Direction, BlockCapabilityCache<EnergyHandler, @Nullable Direction>> energyCapabilityCaches = new EnumMap<>(Direction.class);

    public TileEntityInductionPort(BlockPos pos, BlockState state) {
        super(MekanismBlocks.INDUCTION_PORT, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected @Nullable IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        //Don't allow inserting if we are on output mode, or extracting if we are on input mode
        //TODO - 26.1: Validate when formed/unformed we invalidate caps, as we now return no container if the multiblock isn't formed
        return ProxiedEnergyContainerHolder.create(_ -> !getActive(), _ -> getActive(), _ -> getMultiblock().getEnergyContainer());
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        //Do not handle energy when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.ENERGY) {
            return false;
        }
        return super.persists(type);
    }

    public void addEnergyTargetCapability(List<CapabilityOutputTarget<EnergyHandler>> outputTargets, Direction side) {
        BlockCapabilityCache<EnergyHandler, @Nullable Direction> cache = energyCapabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.ENERGY.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            energyCapabilityCaches.put(side, cache);
        }
        outputTargets.add(new CapabilityOutputTarget<>(cache, this::getActive));
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendOverlayMessage(MekanismLang.INDUCTION_PORT_MODE.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true)));
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