package mekanism.common.tile.component;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IMultiTypeCapability;
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
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    private final EnumColor[] inputColors = new EnumColor[EnumUtils.SIDES.length];
    private final LongSupplier chemicalEjectRate;
    private final IntSupplier fluidEjectRate;
    @Nullable
    private final FloatingLongSupplier energyEjectRate;
    @Nullable
    private Predicate<TransmissionType> canEject;
    @Nullable//TODO: At some point it would be nice to be able to generify this further
    private Predicate<IChemicalTank<?, ?>> canTankEject;
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

    public TileComponentEjector(TileEntityMekanism tile, FloatingLongSupplier energyEjectRate) {
        this(tile, MekanismConfig.general.chemicalAutoEjectRate, MekanismConfig.general.fluidAutoEjectRate, energyEjectRate);
    }

    public TileComponentEjector(TileEntityMekanism tile, LongSupplier chemicalEjectRate, IntSupplier fluidEjectRate, @Nullable FloatingLongSupplier energyEjectRate) {
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

    public TileComponentEjector setCanTankEject(Predicate<IChemicalTank<?, ?>> canTankEject) {
        this.canTankEject = canTankEject;
        return this;
    }

    public boolean isEjecting(ConfigInfo info, TransmissionType type) {
        return info.isEjecting() && (canEject == null || canEject.test(type));
    }

    public void tickServer() {
        for (Map.Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            if (isEjecting(info, type)) {
                if (type == TransmissionType.ITEM) {
                    if (tickDelay == 0) {
                        outputItems(info);
                    } else {
                        tickDelay--;
                    }
                } else if (type != TransmissionType.HEAT) {
                    eject(type, info);
                }
            }
        }
    }

    /**
     * @apiNote Ensure that it can eject before calling this method.
     */
    private void eject(TransmissionType type, ConfigInfo info) {
        //Used to keep track of tanks to what sides they output to
        Map<Object, Set<Direction>> outputData = null;
        for (DataType dataType : info.getSupportedDataTypes()) {
            if (dataType.canOutput()) {
                ISlotInfo slotInfo = info.getSlotInfo(dataType);
                if (slotInfo != null) {
                    Set<Direction> outputSides = info.getSidesForData(dataType);
                    if (!outputSides.isEmpty()) {
                        if (outputData == null) {
                            //Lazy init outputData
                            outputData = new HashMap<>();
                        }
                        if (type.isChemical() && slotInfo instanceof ChemicalSlotInfo<?, ?, ?> chemicalSlotInfo) {
                            for (IChemicalTank<?, ?> tank : chemicalSlotInfo.getTanks()) {
                                if (!tank.isEmpty() && (canTankEject == null || canTankEject.test(tank))) {
                                    outputData.computeIfAbsent(tank, t -> EnumSet.noneOf(Direction.class)).addAll(outputSides);
                                }
                            }
                        } else if (type == TransmissionType.FLUID && slotInfo instanceof FluidSlotInfo fluidSlotInfo) {
                            for (IExtendedFluidTank tank : fluidSlotInfo.getTanks()) {
                                if (!tank.isEmpty()) {
                                    outputData.computeIfAbsent(tank, t -> EnumSet.noneOf(Direction.class)).addAll(outputSides);
                                }
                            }
                        } else if (type == TransmissionType.ENERGY && slotInfo instanceof EnergySlotInfo energySlotInfo) {
                            for (IEnergyContainer container : energySlotInfo.getContainers()) {
                                if (!container.isEmpty()) {
                                    outputData.computeIfAbsent(container, t -> EnumSet.noneOf(Direction.class)).addAll(outputSides);
                                }
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
                if (type.isChemical()) {
                    emit(level, pos, typeCapabilityCaches, sides, (IChemicalTank<?, ?>) entry.getKey());
                } else if (type == TransmissionType.FLUID) {
                    List<BlockCapabilityCache<IFluidHandler, @Nullable Direction>> caches = getCapabilityCaches(level, pos, typeCapabilityCaches, sides, Capabilities.FLUID);
                    FluidUtils.emit(caches, (IExtendedFluidTank) entry.getKey(), fluidEjectRate.getAsInt());
                } else if (type == TransmissionType.ENERGY) {
                    IEnergyContainer container = (IEnergyContainer) entry.getKey();
                    CableUtils.emit(sides, container, tile, energyEjectRate == null ? container.getMaxEnergy() : energyEjectRate.get());
                }
            }
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void emit( ServerLevel level, BlockPos pos,
          Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches, Set<Direction> sides, IChemicalTank<CHEMICAL, STACK> tank) {
        List<BlockCapabilityCache<HANDLER, @Nullable Direction>> caches = getCapabilityCaches(level, pos, typeCapabilityCaches, sides, ChemicalUtil.getCapabilityForChemical(tank));
        ChemicalUtil.emit(caches, tank, chemicalEjectRate.getAsLong());
    }

    @SuppressWarnings("unchecked")
    private static <HANDLER> List<BlockCapabilityCache<HANDLER, @Nullable Direction>> getCapabilityCaches(ServerLevel level, BlockPos pos, Map<Direction, BlockCapabilityCache<?, @Nullable Direction>> typeCapabilityCaches,
          Set<Direction> sides, IMultiTypeCapability<HANDLER, ?> capability) {
        List<BlockCapabilityCache<HANDLER, @Nullable Direction>> caches = new ArrayList<>(sides.size());
        for (Direction side : sides) {
            caches.add((BlockCapabilityCache<HANDLER, @Nullable Direction>) typeCapabilityCaches.computeIfAbsent(side, s -> capability.createCache(level, pos.relative(s), s.getOpposite())));
        }
        return caches;
    }

    /**
     * @apiNote Ensure that it can eject before calling this method.
     */
    private void outputItems(ConfigInfo info) {
        for (DataType dataType : info.getSupportedDataTypes()) {
            if (!dataType.canOutput()) {
                continue;
            }
            ISlotInfo slotInfo = info.getSlotInfo(dataType);
            if (slotInfo instanceof InventorySlotInfo inventorySlotInfo) {
                //Validate the slot info is of the correct type
                Set<Direction> outputs = info.getSidesForData(dataType);
                if (!outputs.isEmpty()) {
                    IItemHandler handler = getHandler(outputs.iterator().next());
                    //NOTE: The below logic and the entire concept of EjectTransitRequest relies on the implementation detail that
                    // per DataType all exposed slots are the same regardless of the actual side. If this ever changes or there are
                    // cases discovered where this is not the case we will instead need to calculate the eject map for each output side
                    // instead of only having to do it once per DataType
                    EjectTransitRequest ejectMap = InventoryUtils.getEjectItemMap(new EjectTransitRequest(handler), inventorySlotInfo.getSlots());
                    if (!ejectMap.isEmpty()) {
                        for (Direction side : outputs) {
                            BlockPos relative = tile.getBlockPos().relative(side);
                            BlockEntity target = WorldUtils.getTileEntity(tile.getLevel(), relative);
                            //Update the handler so that if/when the response uses it, it makes sure it is using the correct side's restrictions
                            ejectMap.handler = getHandler(side);
                            //If the spot is not loaded just skip trying to eject to it
                            TransitResponse response = ejectMap.eject(tile, relative, target, side, 0, transporter -> outputColor);
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
        }

        tickDelay = MekanismUtils.TICKS_PER_HALF_SECOND;
    }

    private IItemHandler getHandler(Direction side) {
        //Note: We can't just pass "tile" and have to instead look up the capability to make sure we respect any sidedness
        return Capabilities.ITEM.getCapabilityIfLoaded(tile.getLevel(), tile.getBlockPos(), null, tile, side);
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
        return NBTConstants.COMPONENT_EJECTOR;
    }

    @Override
    public void deserialize(CompoundTag ejectorNBT) {
        deserialize(ejectorNBT, strict -> strictInput = strict, output -> outputColor = output, inputColors);
    }

    public static void deserialize(CompoundTag ejectorNBT, BooleanConsumer strictInputSetter, Consumer<EnumColor> outputColorSetter, EnumColor[] inputColors) {
        strictInputSetter.accept(ejectorNBT.getBoolean(NBTConstants.STRICT_INPUT));
        outputColorSetter.accept(NBTUtils.getEnum(ejectorNBT, NBTConstants.COLOR, TransporterUtils::readColor));
        //Input colors
        if (ejectorNBT.contains(NBTConstants.INPUT_COLOR, Tag.TAG_INT_ARRAY)) {
            int[] colors = ejectorNBT.getIntArray(NBTConstants.INPUT_COLOR);
            for (int i = 0; i < colors.length && i < inputColors.length; i++) {
                inputColors[i] = TransporterUtils.readColor(colors[i]);
            }
        } else {//TODO - 1.21?: Remove this legacy way of reading it, and use the commented line instead
            //Arrays.fill(inputColors, null);
            for (int i = 0; i < inputColors.length; i++) {
                inputColors[i] = NBTUtils.getEnum(ejectorNBT, NBTConstants.COLOR + i, TransporterUtils::readColor);
            }
        }
    }

    @Override
    public CompoundTag serialize() {
        return serialize(strictInput, inputColors, outputColor);
    }

    public static CompoundTag serialize(boolean strictInput, EnumColor[] inputColors, @Nullable EnumColor outputColor) {
        CompoundTag ejectorNBT = new CompoundTag();
        if (strictInput) {
            ejectorNBT.putBoolean(NBTConstants.STRICT_INPUT, true);
        }
        if (outputColor != null) {
            NBTUtils.writeEnum(ejectorNBT, NBTConstants.COLOR, outputColor);
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
            ejectorNBT.putIntArray(NBTConstants.INPUT_COLOR, colors);
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
        }

        @Override
        protected IItemHandler getHandler() {
            return handler;
        }
    }
}
