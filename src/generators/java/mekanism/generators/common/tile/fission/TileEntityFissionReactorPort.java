package mekanism.generators.common.tile.fission;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.IContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.lib.multiblock.MultiblockData.AdvancedCapabilityOutputTarget;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode.FissionPortMode;
import mekanism.generators.common.registries.GeneratorsBlocks;
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

public class TileEntityFissionReactorPort extends TileEntityFissionReactorCasing {

    private final Map<Direction, BlockCapabilityCache<ResourceHandler<ChemicalResource>, @Nullable Direction>> capabilityCaches = new EnumMap<>(Direction.class);
    private final List<BlockCapability<?, @Nullable Direction>> portCapabilities = List.of(
          Capabilities.CHEMICAL.block(),
          Capabilities.FLUID.block()
    );
    private final Predicate<FissionPortMode> MODE_MATCHES = mode -> mode == getMode();

    public TileEntityFissionReactorPort(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FISSION_REACTOR_PORT, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@NotNull Direction side) {
        if (canHandleHeat() && getHeatCapacitorCount(side) > 0) {
            if (WorldUtils.getBlockState(level, getBlockPos().relative(side))
                  .filter(state -> !state.is(GeneratorsBlocks.FISSION_REACTOR_PORT))
                  .isPresent()) {
                return getAdjacentUnchecked(side);
            }
        }
        return null;
    }

    @NotNull
    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        return _ -> getMultiblock().getChemicalTanks(getMode());
    }

    @NotNull
    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        return _ -> getMode() == FissionPortMode.INPUT ? getMultiblock().getValveFluidTanks(getBlockPos()) : Collections.emptyList();
    }

    @NotNull
    @Override
    protected IContainerHolder<IHeatCapacitor> getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return _ -> getMultiblock().getHeatCapacitors();
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        if (type == ContainerType.HEAT || type == ContainerType.CHEMICAL || type == ContainerType.FLUID) {
            return false;
        }
        return super.persists(type);
    }

    public void addChemicalTargetCapability(List<AdvancedCapabilityOutputTarget<ResourceHandler<ChemicalResource>, FissionPortMode>> outputTargets, Direction side) {
        BlockCapabilityCache<ResourceHandler<ChemicalResource>, @Nullable Direction> cache = capabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.CHEMICAL.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            capabilityCaches.put(side, cache);
        }
        outputTargets.add(new AdvancedCapabilityOutputTarget<>(cache, MODE_MATCHES));
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
            player.sendOverlayMessage(MekanismLang.BOILER_VALVE_MODE_CHANGE.translateColored(EnumColor.GRAY, mode));
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
    void incrementMode() {
        setMode(getMode().getNext());
    }

    @ComputerMethod
    void decrementMode() {
        setMode(getMode().getPrevious());
    }
    //End methods IComputerTile
}