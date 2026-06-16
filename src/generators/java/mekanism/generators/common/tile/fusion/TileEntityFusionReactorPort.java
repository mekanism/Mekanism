package mekanism.generators.common.tile.fusion;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.MultiblockData.CapabilityOutputTarget;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

public class TileEntityFusionReactorPort extends TileEntityFusionReactorBlock {

    private final Map<Direction, BlockCapabilityCache<ResourceHandler<ChemicalResource>, @Nullable Direction>> chemicalCapabilityCaches = new EnumMap<>(Direction.class);
    private final Map<Direction, BlockCapabilityCache<EnergyHandler, @Nullable Direction>> energyCapabilityCaches = new EnumMap<>(Direction.class);

    public TileEntityFusionReactorPort(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FUSION_REACTOR_PORT, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        //Note: We can just use a proxied holder as the input/output restrictions are done in the tanks themselves
        return _ -> getMultiblock().getChemicalTanks();
    }

    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        return _ -> getMultiblock().getFluidTanks();
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        return _ -> getMultiblock().getEnergyContainer();
    }

    @Override
    protected ISingleContainerHolder<IHeatCapacitor> getInitialHeatCapacitor(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return _ -> getMultiblock().getHeatCapacitor();
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        if (type == ContainerType.CHEMICAL || type == ContainerType.FLUID || type == ContainerType.ENERGY || type == ContainerType.HEAT) {
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

    public void addEnergyTargetCapability(List<CapabilityOutputTarget<EnergyHandler>> outputTargets, Direction side) {
        BlockCapabilityCache<EnergyHandler, @Nullable Direction> cache = energyCapabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.ENERGY.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            energyCapabilityCaches.put(side, cache);
        }
        outputTargets.add(new CapabilityOutputTarget<>(cache, this::getActive));
    }

    @Override
    public InteractionResult onSneakRightClick(Level level, Player player) {
        if (!level.isClientSide()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendOverlayMessage(GeneratorsLang.REACTOR_PORT_EJECT.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true)));
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

    @ComputerMethod(methodDescription = "true -> output, false -> input")
    boolean getMode() {
        return getActive();
    }

    @ComputerMethod(methodDescription = "true -> output, false -> input")
    void setMode(boolean output) {
        setActive(output);
    }
    //End methods IComputerTile
}
