package mekanism.common.tile.multiblock;

import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.AttributeStateCommonValveMode;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TileEntityDynamicValve extends TileEntityDynamicTank {

    private final Map<Direction, BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> capabilityCachesChemical = new EnumMap<>(Direction.class);
    private final Map<Direction, BlockCapabilityCache<IFluidHandler, @Nullable Direction>> capabilityCachesFluid = new EnumMap<>(Direction.class);
    private final List<BlockCapability<?, @Nullable Direction>> portCapabilities = List.of(
            Capabilities.CHEMICAL.block(),
            Capabilities.FLUID.block()
    );

    private final Predicate<AttributeStateCommonValveMode.CommonValveMode> MODE_MATCHES = mode -> mode == getMode();

    public TileEntityDynamicValve(BlockPos pos, BlockState state) {
        super(MekanismBlocks.DYNAMIC_VALVE, pos, state);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> getMultiblock().getFluidTanks(getMode());
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        return side -> getMultiblock().getChemicalTanks(getMode());
    }
    
    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.FLUID || type == ContainerType.CHEMICAL) {
            return false;
        }
        return super.persists(type);
    }

    // TODO figure a cleaner universal way for this
    public void addValveTargetCapability(
            List<MultiblockData.AdvancedCapabilityOutputTarget<IChemicalHandler,AttributeStateCommonValveMode.CommonValveMode>> outputTargetsChemical,
            List<MultiblockData.AdvancedCapabilityOutputTarget<IFluidHandler,AttributeStateCommonValveMode.CommonValveMode>> outputTargetsFluid,
            Direction side) {

        // chemical
        BlockCapabilityCache<IChemicalHandler, @Nullable Direction> cacheChemical = capabilityCachesChemical.get(side);
        if (cacheChemical == null) {
            cacheChemical = Capabilities.CHEMICAL.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            capabilityCachesChemical.put(side, cacheChemical);
        }

        outputTargetsChemical.add(new MultiblockData.AdvancedCapabilityOutputTarget<>(cacheChemical, MODE_MATCHES));

        // fluid
        BlockCapabilityCache<IFluidHandler, @Nullable Direction> cacheFluid = capabilityCachesFluid.get(side);
        if (cacheFluid == null) {
            cacheFluid = Capabilities.FLUID.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            capabilityCachesFluid.put(side, cacheFluid);
        }

        outputTargetsFluid.add(new MultiblockData.AdvancedCapabilityOutputTarget<>(cacheFluid, MODE_MATCHES));
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }

    @ComputerMethod(methodDescription = "Get the current configuration of this valve")
    AttributeStateCommonValveMode.CommonValveMode getMode() {
        return getBlockState().getValue(AttributeStateCommonValveMode.modeProperty);
    }

    @ComputerMethod(methodDescription = "Change the configuration of this valve")
    void setMode(AttributeStateCommonValveMode.CommonValveMode mode) {
        if (mode != getMode()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AttributeStateCommonValveMode.modeProperty, mode));
            invalidateCapabilitiesAll(portCapabilities);
        }
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            AttributeStateCommonValveMode.CommonValveMode mode = getMode().getNext();
            setMode(mode);
            player.displayClientMessage(MekanismLang.COMMON_VALVE_MODE_CHANGE.translateColored(EnumColor.GRAY, mode), true);
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

    @ComputerMethod(methodDescription = "Toggle the current valve configuration to the next option in the list")
    void incrementMode() {
        setMode(getMode().getNext());
    }

    @ComputerMethod(methodDescription = "Toggle the current valve configuration to the previous option in the list")
    void decrementMode() {
        setMode(getMode().getPrevious());
    }

}