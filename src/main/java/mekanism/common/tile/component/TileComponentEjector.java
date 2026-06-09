package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.text.EnumColor;
import mekanism.common.component.component.AttachedEjector;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
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
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class TileComponentEjector implements ITileComponent, ISpecificContainerTracker {

    private final TileEntityMekanism tile;
    private final Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);

    private final Map<TransmissionType, Map<Direction, BlockCapabilityCache<?, @Nullable Direction>>> capabilityCaches = new EnumMap<>(TransmissionType.class);

    private final @Nullable EnumColor[] inputColors = new EnumColor[EnumUtils.SIDES.length];
    private final IntSupplier chemicalEjectRate;
    private final IntSupplier fluidEjectRate;
    @Nullable
    private final IntSupplier energyEjectRate;
    @Nullable
    private Predicate<TransmissionType> canEject;
    @Nullable//TODO: At some point it would be nice to be able to generify this further
    private Predicate<IChemicalTank> canTankEject;
    private boolean strictInput;
    @Nullable
    private EnumColor outputColor;
    private int tickDelay = 0;

    public TileComponentEjector(TileEntityMekanism tile) {
        this(tile, MekanismConfig.general.chemicalAutoEjectRate);
    }

    public TileComponentEjector(TileEntityMekanism tile, IntSupplier chemicalEjectRate) {
        this(tile, chemicalEjectRate, MekanismConfig.general.fluidAutoEjectRate);
    }

    public TileComponentEjector(TileEntityMekanism tile, IntSupplier chemicalEjectRate, IntSupplier fluidEjectRate) {
        this(tile, chemicalEjectRate, fluidEjectRate, null);
    }

    public TileComponentEjector(TileEntityMekanism tile, IntSupplier energyEjectRate, boolean energyMarker) {
        this(tile, MekanismConfig.general.chemicalAutoEjectRate, MekanismConfig.general.fluidAutoEjectRate, energyEjectRate);
    }

    public TileComponentEjector(TileEntityMekanism tile, IntSupplier chemicalEjectRate, IntSupplier fluidEjectRate, @Nullable IntSupplier energyEjectRate) {
        this.tile = tile;
        this.chemicalEjectRate = chemicalEjectRate;
        this.fluidEjectRate = fluidEjectRate;
        this.energyEjectRate = energyEjectRate;
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

    public void tickServer(@Nullable TransactionContext transaction) {
        //loop on array to avoid iterator usage and high memory consumption
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            ConfigInfo info = configInfo.get(type);
            if (info == null) {
                continue;
            }
            if (isEjecting(info, type)) {
                if (type == TransmissionType.ITEM) {
                    if (tickDelay == 0) {
                        outputItems(tile.facingSupplier.get(), info, transaction);
                    } else {
                        tickDelay--;
                    }
                } else if (type != TransmissionType.HEAT) {
                    eject(type, tile.facingSupplier.get(), info, transaction);
                }
            }
        }
    }

    private void addData(Map<Object, Set<Direction>> outputData, @Nullable Object container, Set<Direction> outputSides) {
        if (container != null) {
            Set<Direction> directions = outputData.get(container);
            if (directions == null) {
                outputSides = EnumSet.copyOf(outputSides);
                outputData.put(container, outputSides);
            } else {
                directions.addAll(outputSides);
            }
        }
    }

    /**
     * @apiNote Ensure that it can eject before calling this method.
     */
    private void eject(TransmissionType type, Direction facing, ConfigInfo info, @Nullable TransactionContext transaction) {
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
                                for (IFluidTank tank : fluidSlotInfo.getTanks()) {
                                    if (!tank.isEmpty()) {
                                        addData(outputData, tank, outputSides);
                                    }
                                }
                            }
                            case EnergySlotInfo energySlotInfo when type == TransmissionType.ENERGY -> addData(outputData, energySlotInfo.getContainer(), outputSides);
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
            Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches = capabilityCaches.computeIfAbsent(type, _ -> new EnumMap<>(Direction.class));
            for (Map.Entry<Object, Set<Direction>> entry : outputData.entrySet()) {
                Set<Direction> sides = entry.getValue();
                switch (type) {
                    case CHEMICAL -> emitResource(level, pos, sides, entry.getKey(), typeCapabilityCaches, Capabilities.CHEMICAL, chemicalEjectRate, transaction);
                    case FLUID -> emitResource(level, pos, sides, entry.getKey(), typeCapabilityCaches, Capabilities.FLUID, fluidEjectRate, transaction);
                    case ENERGY -> {
                        List<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> caches = initializeCaches(level, pos, sides, typeCapabilityCaches, Capabilities.ENERGY);
                        IEnergyContainer container = (IEnergyContainer) entry.getKey();
                        EnergyUtils.emit(caches, container, energyEjectRate == null ? container.getAmountAsInt() : energyEjectRate.getAsInt(), transaction);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <RESOURCE extends Resource> void emitResource(ServerLevel level, BlockPos pos, Set<Direction> sides, Object container,
          Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches, MultiTypeCapability<ResourceHandler<RESOURCE>> capability, IntSupplier ejectRate,
          @Nullable TransactionContext transaction) {
        List<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> caches = initializeCaches(level, pos, sides, typeCapabilityCaches, capability);
        ResourceUtils.emit(caches, (IResourceContainer<RESOURCE>) container, ejectRate.getAsInt(), transaction);
    }

    private <TYPE> List<BlockCapabilityCache<TYPE, @Nullable Direction>> initializeCaches(ServerLevel level, BlockPos pos, Set<Direction> sides,
          Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches, MultiTypeCapability<TYPE> capability) {
        List<BlockCapabilityCache<TYPE, @Nullable Direction>> caches = new ArrayList<>(sides.size());
        for (Direction side : sides) {
            caches.add(initializeCache(level, pos, side, typeCapabilityCaches, capability));
        }
        return caches;
    }

    @SuppressWarnings("unchecked")
    private <TYPE> BlockCapabilityCache<TYPE, @Nullable Direction> initializeCache(ServerLevel level, BlockPos pos, Direction side,
          Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches, MultiTypeCapability<TYPE> capability) {
        BlockCapabilityCache<TYPE, @Nullable Direction> cache = (BlockCapabilityCache<TYPE, @Nullable Direction>) typeCapabilityCaches.get(side);
        if (cache == null) {
            cache = capability.createCache(level, pos.relative(side), side.getOpposite());
            typeCapabilityCaches.put(side, cache);
        }
        return cache;
    }

    /**
     * @apiNote Ensure that it can eject before calling this method.
     */
    private void outputItems(Direction facing, ConfigInfo info, @Nullable TransactionContext transaction) {
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
                        ResourceHandler<ItemResource> capability = initializeCache(level, tile.getBlockPos(), side, typeCapabilityCaches, Capabilities.ITEM).getCapability();
                        if (capability == null) {
                            //Skip sides where there isn't a target
                            continue;
                        }
                        ResourceHandler<ItemResource> handler = getHandler(side);
                        if (ejectMap == null) {
                            //NOTE: The below logic and the entire concept of EjectTransitRequest relies on the implementation detail that
                            // per DataType all exposed slots are the same regardless of the actual side. If this ever changes or there are
                            // cases discovered where this is not the case we will instead need to calculate the eject map for each output side
                            // instead of only having to do it once per DataType
                            ejectMap = InventoryUtils.getEjectItemMap(new EjectTransitRequest(handler), inventorySlotInfo.getSlots(), transaction);
                            //No items to eject, exit
                            if (ejectMap.isEmpty()) {
                                break;
                            }
                        } else {
                            //Update the handler so that if/when the response uses it, it makes sure it is using the correct side's restrictions
                            ejectMap.setHandler(handler);
                        }
                        //If the spot is not loaded just skip trying to eject to it
                        try (Transaction subTransaction = Transaction.open(transaction)) {
                            TransitResponse response = ejectMap.eject(tile, capability, 1, this.outputColor, subTransaction);
                            if (response.useAll(subTransaction)) {
                                // use the items returned by the TransitResponse; will be visible next loop
                                subTransaction.commit();
                                if (ejectMap.isEmpty()) {
                                    //If we are out of items to eject, break
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        tickDelay = MekanismUtils.TICKS_PER_HALF_SECOND;
    }

    private Set<Direction> getSidesForData(ConfigInfo info, Direction facing, DataType dataType) {
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

    @Nullable
    private ResourceHandler<ItemResource> getHandler(Direction side) {
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

    @Nullable
    @ComputerMethod
    public EnumColor getOutputColor() {
        return outputColor;
    }

    public void setOutputColor(@Nullable EnumColor color) {
        if (outputColor != color) {
            outputColor = color;
            tile.markForSave();
        }
    }

    public boolean isInputSideEnabled(RelativeSide side) {
        ConfigInfo info = configInfo.get(TransmissionType.ITEM);
        return info == null || info.isSideEnabled(side);
    }

    public void setInputColor(RelativeSide side, @Nullable EnumColor color) {
        if (isInputSideEnabled(side)) {
            int ordinal = side.ordinal();
            if (inputColors[ordinal] != color) {
                inputColors[ordinal] = color;
                tile.markForSave();
            }
        }
    }

    @Nullable
    @ComputerMethod
    public EnumColor getInputColor(RelativeSide side) {
        return inputColors[side.ordinal()];
    }

    @Override
    public String getComponentKey() {
        return SerializationConstants.COMPONENT_EJECTOR;
    }

    @Override
    public void applyImplicitComponents(DataComponentGetter input) {
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
    public void deserialize(ValueInput ejectorInput) {
        strictInput = ejectorInput.getBooleanOr(SerializationConstants.STRICT_INPUT, strictInput);
        outputColor = NBTUtils.getEnum(ejectorInput, SerializationConstants.COLOR, EnumColor.BY_ID);
        //Input colors
        Optional<int[]> optionalColors = ejectorInput.getIntArray(SerializationConstants.INPUT_COLOR);
        if (optionalColors.isPresent()) {
            int[] colors = optionalColors.get();
            for (int i = 0; i < colors.length && i < inputColors.length; i++) {
                inputColors[i] = TransporterUtils.readColor(colors[i]);
            }
        } else {
            Arrays.fill(inputColors, null);
        }
    }

    @Override
    public void serialize(ValueOutput ejectorOutput) {
        if (strictInput) {
            ejectorOutput.putBoolean(SerializationConstants.STRICT_INPUT, true);
        }
        if (outputColor != null) {
            NBTUtils.writeEnum(ejectorOutput, SerializationConstants.COLOR, outputColor);
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
            ejectorOutput.putIntArray(SerializationConstants.INPUT_COLOR, colors);
        }
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

        public EjectTransitRequest(@Nullable ResourceHandler<ItemResource> handler) {
            super(handler);
        }

        protected void setHandler(@Nullable ResourceHandler<ItemResource> handler) {
            this.handler = handler;
        }
    }
}
