package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.text.EnumColor;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IMultiTypeCapability;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.lib.inventory.HandlerTransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileComponentEjector implements ITileComponent, ISpecificContainerTracker {

    private final TileEntityMekanism tile;
    private final Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);

    private final Map<TransmissionType, Map<Direction, BlockCapabilityCache<?, @Nullable Direction>>> capabilityCaches = new EnumMap<>(TransmissionType.class);
    private final Map<Direction, BlockEnergyCapabilityCache> energyCapabilityCache = new EnumMap<>(Direction.class);

    private final Function<LogisticalTransporterBase, EnumColor> outputColorFunction;
    private final EnumColor[] inputColors = new EnumColor[EnumUtils.SIDES.length];
    private final LongSupplier chemicalEjectRate;
    private final IntSupplier fluidEjectRate;
    @Nullable
    private final LongSupplier energyEjectRate;
    @Nullable
    private Predicate<TransmissionType> canEject;
    @Nullable//TODO: At some point it would be nice to be able to generify this further
    private Predicate<IChemicalTank> canTankEject;
    private boolean strictInput;
    private EnumColor outputColor;
    private int tickDelay = 0;

    public TileComponentEjector(TileEntityMekanism tile) {
        this(tile, MekanismConfig.general.chemicalAutoEjectRate);
    }

    public TileComponentEjector(TileEntityMekanism tile, LongSupplier chemicalEjectRate) {
        this(tile, chemicalEjectRate, MekanismConfig.general.fluidAutoEjectRate);
    }

    public TileComponentEjector(TileEntityMekanism tile, LongSupplier chemicalEjectRate, IntSupplier fluidEjectRate) {
        this(tile, chemicalEjectRate, fluidEjectRate, null);
    }

    public TileComponentEjector(TileEntityMekanism tile, LongSupplier energyEjectRate, boolean energyMarker) {
        this(tile, MekanismConfig.general.chemicalAutoEjectRate, MekanismConfig.general.fluidAutoEjectRate, energyEjectRate);
    }

    public TileComponentEjector(TileEntityMekanism tile, LongSupplier chemicalEjectRate, IntSupplier fluidEjectRate, @Nullable LongSupplier energyEjectRate) {
        this.tile = tile;
        this.chemicalEjectRate = chemicalEjectRate;
        this.fluidEjectRate = fluidEjectRate;
        this.energyEjectRate = energyEjectRate;
        this.outputColorFunction = transporter -> this.outputColor;
        tile.addComponent(this);
    }

    public TileComponentEjector setOutputData(TileComponentConfig config, TransmissionType... types) {
        for (TransmissionType type : types) {
            ConfigInfo info = config.getConfig(type);
            if (info != null) {
                configInfo.put(type, info);
            }
        }
        return this;
    }

    public TileComponentEjector setCanEject(Predicate<TransmissionType> canEject) {
        this.canEject = canEject;
        return this;
    }

    public TileComponentEjector setCanTankEject(Predicate<IChemicalTank> canTankEject) {
        this.canTankEject = canTankEject;
        return this;
    }

    public boolean isEjecting(ConfigInfo info, TransmissionType type) {
        return info.isEjecting() && (canEject == null || canEject.test(type));
    }

    public void tickServer() {
        //loop on array to avoid iterator usage and high memory consumption
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            ConfigInfo info = configInfo.get(type);
            if (info == null) {
                continue;
            }
            if (isEjecting(info, type)) {
                if (type == TransmissionType.ITEM) {
                    if (tickDelay == 0) {
                        outputItems(tile.facingSupplier.get(), info);
                    } else {
                        tickDelay--;
                    }
                } else if (type != TransmissionType.HEAT) {
                    eject(type, tile.facingSupplier.get(), info);
                }
            }
        }
    }

    private void addData(Map<Object, Set<Direction>> outputData, Object container, Set<Direction> outputSides) {
        Set<Direction> directions = outputData.get(container);
        if (directions == null) {
            outputSides = EnumSet.copyOf(outputSides);
            outputData.put(container, outputSides);
        } else {
            directions.addAll(outputSides);
        }
    }

    /**
     * @apiNote Ensure that it can eject before calling this method.
     */
    private void eject(TransmissionType type, Direction facing, ConfigInfo info) {
        //Used to keep track of tanks to what sides they output to
        Map<Object, Set<Direction>> outputData = null;//todo what is the point of putting it into a map??
        for (DataType dataType : info.getSupportedDataTypes()) {
            if (dataType.canOutput()) {
                ISlotInfo slotInfo = info.getSlotInfo(dataType);
                if (slotInfo != null && !slotInfo.isEmpty()) {//Only bother getting caps if the containers are not empty
                    Set<Direction> outputSides = getSidesForData(info, facing, dataType);
                    if (!outputSides.isEmpty()) {
                        if (outputData == null) {
                            //Lazy init outputData, we use an identity hashmap to allow for cheaper compare checks
                            outputData = new IdentityHashMap<>();
                        }
                        switch (slotInfo) {
                            case ChemicalSlotInfo chemicalSlotInfo when type == TransmissionType.CHEMICAL -> {
                                for (IChemicalTank tank : chemicalSlotInfo.getTanks()) {
                                    if (!tank.isEmpty() && (canTankEject == null || canTankEject.test(tank))) {
                                        addData(outputData, tank, outputSides);
                                    }
                                }
                            }
                            case FluidSlotInfo fluidSlotInfo when type == TransmissionType.FLUID -> {
                                for (IExtendedFluidTank tank : fluidSlotInfo.getTanks()) {
                                    if (!tank.isEmpty()) {
                                        addData(outputData, tank, outputSides);
                                    }
                                }
                            }
                            case EnergySlotInfo energySlotInfo when type == TransmissionType.ENERGY -> {
                                for (IEnergyContainer container : energySlotInfo.getContainers()) {
                                    if (!container.isEmpty()) {
                                        addData(outputData, container, outputSides);
                                    }
                                }
                            }
                            default -> {
                            }
                        }
                    }
                }
            }
        }
        if (outputData != null && !outputData.isEmpty()) {
            ServerLevel level = (ServerLevel) tile.getLevel();
            BlockPos pos = tile.getBlockPos();
            Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches = capabilityCaches.computeIfAbsent(type, t -> new EnumMap<>(Direction.class));
            for (Map.Entry<Object, Set<Direction>> entry : outputData.entrySet()) {
                Set<Direction> sides = entry.getValue();
                switch (type) {
                    case CHEMICAL -> {
                        IChemicalTank tank = (IChemicalTank) entry.getKey();
                        List<BlockCapabilityCache<IChemicalHandler, @Nullable Direction>> caches = getCapabilityCaches(level, pos, typeCapabilityCaches, sides, Capabilities.CHEMICAL);
                        ChemicalUtil.emit(caches, tank, chemicalEjectRate.getAsLong());
                    }
                    case FLUID -> {
                        List<BlockCapabilityCache<IFluidHandler, @Nullable Direction>> caches = getCapabilityCaches(level, pos, typeCapabilityCaches, sides, Capabilities.FLUID);
                        FluidUtils.emit(caches, (IExtendedFluidTank) entry.getKey(), fluidEjectRate.getAsInt());
                    }
                    case ENERGY -> {
                        IEnergyContainer container = (IEnergyContainer) entry.getKey();
                        List<BlockEnergyCapabilityCache> caches = new ArrayList<>(sides.size());
                        for (Direction side : sides) {
                            BlockEnergyCapabilityCache cache = energyCapabilityCache.get(side);
                            if (cache == null) {
                                cache = BlockEnergyCapabilityCache.create(level, pos.relative(side), side.getOpposite());
                                energyCapabilityCache.put(side, cache);
                            }
                            caches.add(cache);
                        }
                        CableUtils.emit(caches, container, energyEjectRate == null ? container.getMaxEnergy() : energyEjectRate.getAsLong());
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <HANDLER> List<BlockCapabilityCache<HANDLER, @Nullable Direction>> getCapabilityCaches(ServerLevel level, BlockPos pos,
          Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches, Set<Direction> sides, IMultiTypeCapability<HANDLER, ?> capability) {
        List<BlockCapabilityCache<HANDLER, @Nullable Direction>> caches = new ArrayList<>(sides.size());
        for (Direction side : sides) {
            BlockCapabilityCache<HANDLER, @Nullable Direction> cache = (BlockCapabilityCache<HANDLER, @Nullable Direction>) typeCapabilityCaches.get(side);
            if (cache == null) {
                cache = capability.createCache(level, pos.relative(side), side.getOpposite());
                typeCapabilityCaches.put(side, cache);
            }
            caches.add(cache);
        }
        return caches;
    }

    /**
     * @apiNote Ensure that it can eject before calling this method.
     */
    private void outputItems(Direction facing, ConfigInfo info) {
        ServerLevel level = (ServerLevel) tile.getLevel();
        Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches = null;
        for (DataType dataType : info.getSupportedDataTypes()) {
            if (!dataType.canOutput()) {
                continue;
            }
            ISlotInfo slotInfo = info.getSlotInfo(dataType);
            if (slotInfo != null && slotInfo.isEmpty()) {
                continue;//don't even bother getting caps etc
            }
            if (slotInfo instanceof InventorySlotInfo inventorySlotInfo) {
                //Validate the slot info is of the correct type
                Set<Direction> outputs = getSidesForData(info, facing, dataType);
                if (!outputs.isEmpty()) {
                    EjectTransitRequest ejectMap = null;
                    if (typeCapabilityCaches == null) {
                        typeCapabilityCaches = capabilityCaches.computeIfAbsent(TransmissionType.ITEM, t -> new EnumMap<>(Direction.class));
                    }
                    for (Direction side : outputs) {
                        BlockCapabilityCache<IItemHandler, @Nullable Direction> cache = (BlockCapabilityCache<IItemHandler, @Nullable Direction>) typeCapabilityCaches.get(side);
                        if (cache == null) {
                            cache = Capabilities.ITEM.createCache(level, tile.getBlockPos().relative(side), side.getOpposite());
                            typeCapabilityCaches.put(side, cache);
                        }
                        IItemHandler capability = cache.getCapability();
                        if (capability == null) {
                            //Skip sides where there isn't a target
                            continue;
                        }
                        IItemHandler handler = getHandler(side);
                        if (ejectMap == null) {
                            //NOTE: The below logic and the entire concept of EjectTransitRequest relies on the implementation detail that
                            // per DataType all exposed slots are the same regardless of the actual side. If this ever changes or there are
                            // cases discovered where this is not the case we will instead need to calculate the eject map for each output side
                            // instead of only having to do it once per DataType
                            ejectMap = InventoryUtils.getEjectItemMap(new EjectTransitRequest(handler), inventorySlotInfo.getSlots());
                            //No items to eject, exit
                            if (ejectMap.isEmpty()) {
                                break;
                            }
                        } else {
                            //Update the handler so that if/when the response uses it, it makes sure it is using the correct side's restrictions
                            ejectMap.handler = handler;
                        }
                        //If the spot is not loaded just skip trying to eject to it
                        TransitResponse response = ejectMap.eject(tile, capability, 0, this.outputColorFunction);
                        if (!response.isEmpty()) {
                            // use the items returned by the TransitResponse; will be visible next loop
                            response.useAll();
                            if (ejectMap.isEmpty()) {
                                //If we are out of items to eject, break
                                break;
                            }
                        }
                    }
                }
            }
        }

        tickDelay = MekanismUtils.TICKS_PER_HALF_SECOND;
    }

    private Set<Direction> getSidesForData(ConfigInfo info, @NotNull Direction facing, @NotNull DataType dataType) {
        Set<Direction> directions = null;
        for (Map.Entry<RelativeSide, DataType> entry : info.getSideConfig()) {
            if (entry.getValue() == dataType) {
                if (directions == null) {
                    //Lazy init the set so that if there are none that match we can just use an empty set
                    // instead of having to initialize an enum set
                    directions = EnumSet.noneOf(Direction.class);
                }
                directions.add(entry.getKey().getDirection(facing));
            }
        }
        return directions == null ? Collections.emptySet() : directions;
    }

    private IItemHandler getHandler(Direction side) {
        //Note: We can't just pass "tile" and have to instead look up the capability to make sure we respect any sidedness
        // we short circuit looking it up from the world though, and just query the provider we add to the tile directly
        return CapabilityTileEntity.ITEM_HANDLER_PROVIDER.getCapability(tile, side);
    }

    @ComputerMethod
    public boolean hasStrictInput() {
        return strictInput;
    }

    public void setStrictInput(boolean strict) {
        if (strictInput != strict) {
            strictInput = strict;
            tile.markForSave();
        }
    }

    @ComputerMethod
    public EnumColor getOutputColor() {
        return outputColor;
    }

    public void setOutputColor(EnumColor color) {
        if (outputColor != color) {
            outputColor = color;
            tile.markForSave();
        }
    }

    public boolean isInputSideEnabled(@NotNull RelativeSide side) {
        ConfigInfo info = configInfo.get(TransmissionType.ITEM);
        return info == null || info.isSideEnabled(side);
    }

    public void setInputColor(RelativeSide side, EnumColor color) {
        if (isInputSideEnabled(side)) {
            int ordinal = side.ordinal();
            if (inputColors[ordinal] != color) {
                inputColors[ordinal] = color;
                tile.markForSave();
            }
        }
    }

    @ComputerMethod
    public EnumColor getInputColor(RelativeSide side) {
        return inputColors[side.ordinal()];
    }

    @Override
    public String getComponentKey() {
        return SerializationConstants.COMPONENT_EJECTOR;
    }

    @Override
    public void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        AttachedEjector ejector = input.get(MekanismDataComponents.EJECTOR);
        if (ejector != null) {
            for (int i = 0; i < inputColors.length; i++) {
                inputColors[i] = ejector.inputColors().get(i).orElse(null);
            }
            strictInput = ejector.strictInput();
            outputColor = ejector.outputColor().orElse(null);
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        builder.set(MekanismDataComponents.EJECTOR, AttachedEjector.create(inputColors, strictInput, outputColor));
    }

    @Override
    public void deserialize(CompoundTag ejectorNBT, HolderLookup.Provider provider) {
        strictInput = ejectorNBT.getBoolean(SerializationConstants.STRICT_INPUT);
        outputColor = NBTUtils.getEnum(ejectorNBT, SerializationConstants.COLOR, EnumColor.BY_ID);
        //Input colors
        if (ejectorNBT.contains(SerializationConstants.INPUT_COLOR, Tag.TAG_INT_ARRAY)) {
            int[] colors = ejectorNBT.getIntArray(SerializationConstants.INPUT_COLOR);
            for (int i = 0; i < colors.length && i < inputColors.length; i++) {
                inputColors[i] = TransporterUtils.readColor(colors[i]);
            }
        } else {
            Arrays.fill(inputColors, null);
        }
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag ejectorNBT = new CompoundTag();
        if (strictInput) {
            ejectorNBT.putBoolean(SerializationConstants.STRICT_INPUT, true);
        }
        if (outputColor != null) {
            NBTUtils.writeEnum(ejectorNBT, SerializationConstants.COLOR, outputColor);
        }
        //Input colors
        int[] colors = new int[inputColors.length];
        boolean hasColor = false;
        for (int i = 0; i < inputColors.length; i++) {
            EnumColor color = inputColors[i];
            colors[i] = TransporterUtils.getColorIndex(color);
            if (color != null) {
                hasColor = true;
            }
        }
        if (hasColor) {
            ejectorNBT.putIntArray(SerializationConstants.INPUT_COLOR, colors);
        }
        return ejectorNBT;
    }

    @Override
    public List<ISyncableData> getSpecificSyncableData() {
        List<ISyncableData> list = new ArrayList<>();
        list.add(SyncableBoolean.create(this::hasStrictInput, input -> strictInput = input));
        list.add(SyncableInt.create(() -> TransporterUtils.getColorIndex(outputColor), index -> outputColor = TransporterUtils.readColor(index)));
        for (int i = 0; i < inputColors.length; i++) {
            int idx = i;
            list.add(SyncableInt.create(() -> TransporterUtils.getColorIndex(inputColors[idx]), index -> inputColors[idx] = TransporterUtils.readColor(index)));
        }
        return list;
    }

    //Computer related methods
    @ComputerMethod(nameOverride = "setStrictInput", requiresPublicSecurity = true)
    void computerSetStrictInput(boolean strict) throws ComputerException {
        tile.validateSecurityIsPublic();
        setStrictInput(strict);
    }

    private void validateInputSide(RelativeSide side) throws ComputerException {
        if (!isInputSideEnabled(side)) {
            throw new ComputerException("Side '%s' is disabled and can't be configured.", side);
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void clearInputColor(RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateInputSide(side);
        setInputColor(side, null);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void incrementInputColor(RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateInputSide(side);
        int ordinal = side.ordinal();
        inputColors[ordinal] = TransporterUtils.increment(inputColors[ordinal]);
        tile.markForSave();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void decrementInputColor(RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateInputSide(side);
        int ordinal = side.ordinal();
        inputColors[ordinal] = TransporterUtils.decrement(inputColors[ordinal]);
        tile.markForSave();
    }

    @ComputerMethod(nameOverride = "setInputColor", requiresPublicSecurity = true)
    void computerSetInputColor(RelativeSide side, EnumColor color) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateInputSide(side);
        setInputColor(side, color);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void clearOutputColor() throws ComputerException {
        tile.validateSecurityIsPublic();
        setOutputColor(null);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void incrementOutputColor() throws ComputerException {
        tile.validateSecurityIsPublic();
        outputColor = TransporterUtils.increment(outputColor);
        tile.markForSave();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void decrementOutputColor() throws ComputerException {
        tile.validateSecurityIsPublic();
        outputColor = TransporterUtils.decrement(outputColor);
        tile.markForSave();
    }

    @ComputerMethod(nameOverride = "setOutputColor", requiresPublicSecurity = true)
    void computerSetOutputColor(EnumColor color) throws ComputerException {
        tile.validateSecurityIsPublic();
        setOutputColor(color);
    }
    //End computer related methods

    private static class EjectTransitRequest extends HandlerTransitRequest {

        public IItemHandler handler;

        public EjectTransitRequest(IItemHandler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        protected IItemHandler getHandler() {
            return handler;
        }
    }
}
