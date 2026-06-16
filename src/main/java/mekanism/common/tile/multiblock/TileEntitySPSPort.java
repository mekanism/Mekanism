package mekanism.common.tile.multiblock;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.ProxiedContainerHolder;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.capabilities.holder.single.ProxiedSingleContainerHolder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class TileEntitySPSPort extends TileEntitySPSCasing {

    private final Map<Direction, BlockCapabilityCache<ResourceHandler<ChemicalResource>, @Nullable Direction>> chemicalCapabilityCaches = new EnumMap<>(Direction.class);
    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntitySPSPort> energyContainer;

    public TileEntitySPSPort(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SPS_PORT, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level, SPSMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(level, multiblock);
        if (multiblock.isFormed()) {
            if (!energyContainer.isEmpty() && multiblock.canSupplyCoilEnergy(this)) {
                try (Transaction transaction = Transaction.openRoot()) {
                    multiblock.supplyCoilEnergy(this, energyContainer.extract(energyContainer.getAmountAsInt(), transaction, AutomationType.INTERNAL));
                    transaction.commit();
                }
            }
        }
        return needsPacket;
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = MachineEnergyContainer.input(this, listener);
        return ProxiedSingleContainerHolder.energy(_ -> !getActive(), _ -> getActive(), _ -> energyContainer);
    }

    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        //Don't allow inserting if we are on output mode, or extracting if we are on input mode, our tanks will handle ensuring that we don't insert/extract from an invalid tank
        return new ProxiedContainerHolder<>(_ -> !getActive(), _ -> getActive(), _ -> getMultiblock().getChemicalTanks());
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        if (type == ContainerType.CHEMICAL) {
            return false;
        }
        return super.persists(type);
    }

    public void addChemicalTargetCapability(List<CapabilityOutputTarget<ResourceHandler<ChemicalResource>>> outputTargets, Direction side) {
        BlockCapabilityCache<ResourceHandler<ChemicalResource>, @Nullable Direction> cache = chemicalCapabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.CHEMICAL.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            chemicalCapabilityCaches.put(side, cache);
        }
        outputTargets.add(new CapabilityOutputTarget<>(cache, this::getActive));
    }

    @Override
    public InteractionResult onSneakRightClick(Level level, Player player) {
        if (!level.isClientSide()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendOverlayMessage(MekanismLang.SPS_PORT_MODE.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true)));
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