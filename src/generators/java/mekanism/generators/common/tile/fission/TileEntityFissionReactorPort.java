package mekanism.generators.common.tile.fission;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.IMultiblockEjector;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode.FissionPortMode;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFissionReactorPort extends TileEntityFissionReactorCasing implements IMultiblockEjector {

    private final Map<Direction, BlockCapabilityCache<IGasHandler, @Nullable Direction>> capabilityCaches = new EnumMap<>(Direction.class);
    private final List<BlockCapabilityCache<IGasHandler, @Nullable Direction>> outputTargets = new ArrayList<>();
    private final List<BlockCapability<?, @Nullable Direction>> portCapabilities = List.of(
          Capabilities.GAS.block(),
          Capabilities.FLUID.block()
    );

    public TileEntityFissionReactorPort(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FISSION_REACTOR_PORT, pos, state);
    }

    @Override
    protected boolean onUpdateServer(FissionReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        if (multiblock.isFormed()) {
            FissionPortMode mode = getMode();
            if (mode == FissionPortMode.OUTPUT_COOLANT) {
                ChemicalUtil.emit(outputTargets, multiblock.heatedCoolantTank);
            } else if (mode == FissionPortMode.OUTPUT_WASTE) {
                ChemicalUtil.emit(outputTargets, multiblock.wasteTank);
            }
        }
        return needsPacket;
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@NotNull Direction side) {
        if (canHandleHeat() && getHeatCapacitorCount(side) > 0) {
            BlockPos pos = getBlockPos().relative(side);
            return WorldUtils.getBlockState(level, pos)
                  .filter(state -> !state.isAir() && state.getBlock() != GeneratorsBlocks.FISSION_REACTOR_PORT.getBlock())
                  //Note: We know the position is loaded already from the blockstate check
                  .map(state -> level.getCapability(Capabilities.HEAT, pos, state, null, side.getOpposite()))
                  .orElse(null);
        }
        return null;
    }

    @NotNull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        return side -> getMultiblock().getGasTanks(getMode());
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> getMode() == FissionPortMode.INPUT ? getMultiblock().getFluidTanks(side) : List.of();
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.HEAT || type == ContainerType.GAS || type == ContainerType.FLUID) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public void setEjectSides(Set<Direction> sides) {
        outputTargets.clear();
        for (Direction side : sides) {
            outputTargets.add(capabilityCaches.computeIfAbsent(side, s -> Capabilities.GAS.createCache((ServerLevel) level, worldPosition.relative(s), s.getOpposite())));
        }
    }

    @ComputerMethod
    FissionPortMode getMode() {
        return getBlockState().getValue(AttributeStateFissionPortMode.modeProperty);
    }

    @ComputerMethod
    void setMode(FissionPortMode mode) {
        if (mode != getMode()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AttributeStateFissionPortMode.modeProperty, mode));
            invalidateCapabilitiesAll(portCapabilities);
        }
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            FissionPortMode mode = getMode().getNext();
            setMode(mode);
            player.displayClientMessage(MekanismLang.BOILER_VALVE_MODE_CHANGE.translateColored(EnumColor.GRAY, mode), true);
        }
        return InteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public FluidStack insertFluid(int tank, @NotNull FluidStack stack, Direction side, @NotNull Action action) {
        return handleValves(stack, action, super.insertFluid(tank, stack, side, action));
    }

    @NotNull
    @Override
    public FluidStack insertFluid(@NotNull FluidStack stack, Direction side, @NotNull Action action) {
        return handleValves(stack, action, super.insertFluid(stack, side, action));
    }

    private FluidStack handleValves(@NotNull FluidStack stack, @NotNull Action action, @NotNull FluidStack remainder) {
        if (action.execute() && remainder.getAmount() < stack.getAmount()) {
            getMultiblock().triggerValveTransfer(this);
        }
        return remainder;
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
    void incrementMode() {
        setMode(getMode().getNext());
    }

    @ComputerMethod
    void decrementMode() {
        setMode(getMode().getPrevious());
    }
    //End methods IComputerTile
}