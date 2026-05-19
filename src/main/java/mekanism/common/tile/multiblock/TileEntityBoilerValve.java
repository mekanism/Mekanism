package mekanism.common.tile.multiblock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode.BoilerValveMode;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.MultiblockData.AdvancedCapabilityOutputTarget;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityBoilerValve extends TileEntityBoilerCasing {

    private final Map<Direction, BlockCapabilityCache<ResourceHandler<ChemicalResource>, @Nullable Direction>> capabilityCaches = new EnumMap<>(Direction.class);
    private final List<BlockCapability<?, @Nullable Direction>> portCapabilities = List.of(
          Capabilities.CHEMICAL.block(),
          Capabilities.FLUID.block()
    );
    private final Predicate<BoilerValveMode> MODE_MATCHES = mode -> mode == getMode();

    public TileEntityBoilerValve(BlockPos pos, BlockState state) {
        super(MekanismBlocks.BOILER_VALVE, pos, state);
        delaySupplier = NO_DELAY;
    }

    @NotNull
    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        return _ -> getMultiblock().getChemicalTanks(getMode());
    }

    @NotNull
    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        return _ -> getMode() == BoilerValveMode.INPUT ? getMultiblock().getValveFluidTanks(getBlockPos()) : Collections.emptyList();
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //Do not handle fluid or gas when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.FLUID || type == ContainerType.CHEMICAL) {
            return false;
        }
        return super.persists(type);
    }

    public void addChemicalTargetCapability(List<AdvancedCapabilityOutputTarget<ResourceHandler<ChemicalResource>, BoilerValveMode>> outputTargets, Direction side) {
        BlockCapabilityCache<ResourceHandler<ChemicalResource>, @Nullable Direction> cache = capabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.CHEMICAL.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            capabilityCaches.put(side, cache);
        }
        outputTargets.add(new AdvancedCapabilityOutputTarget<>(cache, MODE_MATCHES));
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }

    @ComputerMethod(methodDescription = "Get the current configuration of this valve")
    BoilerValveMode getMode() {
        return getBlockState().getValue(AttributeStateBoilerValveMode.modeProperty);
    }

    @ComputerMethod(methodDescription = "Change the configuration of this valve")
    void setMode(BoilerValveMode mode) {
        if (mode != getMode()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AttributeStateBoilerValveMode.modeProperty, mode));
            invalidateCapabilitiesAll(portCapabilities);
        }
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        if (!isRemote()) {
            BoilerValveMode mode = getMode().getNext();
            setMode(mode);
            player.sendOverlayMessage(MekanismLang.BOILER_VALVE_MODE_CHANGE.translateColored(EnumColor.GRAY, mode));
        }
        return InteractionResult.SUCCESS;
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = "Toggle the current valve configuration to the next option in the list")
    void incrementMode() {
        setMode(getMode().getNext());
    }

    @ComputerMethod(methodDescription = "Toggle the current valve configuration to the previous option in the list")
    void decrementMode() {
        setMode(getMode().getPrevious());
    }
    //End methods IComputerTile
}
