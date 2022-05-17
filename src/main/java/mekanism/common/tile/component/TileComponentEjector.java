package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.lib.inventory.TileTransitRequest;
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
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileComponentEjector implements ITileComponent, ISpecificContainerTracker {

    private final TileEntityMekanism tile;
    private final Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
    private final EnumColor[] inputColors = new EnumColor[6];
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

    public void tickServer() {
        for (Map.Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            if (info.isEjecting() && (canEject == null || canEject.test(type))) {
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
            for (Map.Entry<Object, Set<Direction>> entry : outputData.entrySet()) {
                if (type.isChemical()) {
                    ChemicalUtil.emit(entry.getValue(), (IChemicalTank<?, ?>) entry.getKey(), tile, chemicalEjectRate.getAsLong());
                } else if (type == TransmissionType.FLUID) {
                    FluidUtils.emit(entry.getValue(), (IExtendedFluidTank) entry.getKey(), tile, fluidEjectRate.getAsInt());
                } else if (type == TransmissionType.ENERGY) {
                    IEnergyContainer container = (IEnergyContainer) entry.getKey();
                    CableUtils.emit(entry.getValue(), container, tile, energyEjectRate == null ? container.getMaxEnergy() : energyEjectRate.get());
                }
            }
        }
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
            if (slotInfo instanceof InventorySlotInfo) {
                //Validate the slot info is of the correct type
                Set<Direction> outputs = info.getSidesForData(dataType);
                if (!outputs.isEmpty()) {
                    EjectTransitRequest ejectMap = InventoryUtils.getEjectItemMap(new EjectTransitRequest(tile, outputs.iterator().next()),
                          ((InventorySlotInfo) slotInfo).getSlots());
                    if (!ejectMap.isEmpty()) {
                        for (Direction side : outputs) {
                            BlockEntity target = WorldUtils.getTileEntity(tile.getLevel(), tile.getBlockPos().relative(side));
                            if (target != null) {
                                //Update the side so that if/when the response uses it, it makes sure it is grabbing from the correct side
                                ejectMap.side = side;
                                //If the spot is not loaded just skip trying to eject to it
                                TransitResponse response;
                                if (target instanceof TileEntityLogisticalTransporterBase transporter) {
                                    response = transporter.getTransmitter().insert(tile, ejectMap, outputColor, true, 0);
                                } else {
                                    response = ejectMap.addToInventory(target, side, 0, false);
                                }
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
        }

        tickDelay = 10;
    }

    @ComputerMethod
    public boolean hasStrictInput() {
        return strictInput;
    }

    public void setStrictInput(boolean strict) {
        if (strictInput != strict) {
            strictInput = strict;
            WorldUtils.saveChunk(tile);
        }
    }

    @ComputerMethod
    public EnumColor getOutputColor() {
        return outputColor;
    }

    public void setOutputColor(EnumColor color) {
        if (outputColor != color) {
            outputColor = color;
            WorldUtils.saveChunk(tile);
        }
    }

    public boolean isInputSideEnabled(@Nonnull RelativeSide side) {
        ConfigInfo info = configInfo.get(TransmissionType.ITEM);
        return info == null || info.isSideEnabled(side);
    }

    public void setInputColor(RelativeSide side, EnumColor color) {
        if (isInputSideEnabled(side)) {
            int ordinal = side.ordinal();
            if (inputColors[ordinal] != color) {
                inputColors[ordinal] = color;
                WorldUtils.saveChunk(tile);
            }
        }
    }

    @ComputerMethod
    public EnumColor getInputColor(RelativeSide side) {
        return inputColors[side.ordinal()];
    }

    @Override
    public void read(CompoundTag nbtTags) {
        NBTUtils.setCompoundIfPresent(nbtTags, NBTConstants.COMPONENT_EJECTOR, ejectorNBT -> {
            strictInput = ejectorNBT.getBoolean(NBTConstants.STRICT_INPUT);
            NBTUtils.setEnumIfPresent(ejectorNBT, NBTConstants.COLOR, TransporterUtils::readColor, color -> outputColor = color);
            //Input colors
            for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
                int index = i;
                NBTUtils.setEnumIfPresent(ejectorNBT, NBTConstants.COLOR + index, TransporterUtils::readColor, color -> inputColors[index] = color);
            }
        });
    }

    @Override
    public void write(CompoundTag nbtTags) {
        CompoundTag ejectorNBT = new CompoundTag();
        ejectorNBT.putBoolean(NBTConstants.STRICT_INPUT, strictInput);
        if (outputColor != null) {
            ejectorNBT.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(outputColor));
        }
        //Input colors
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            ejectorNBT.putInt(NBTConstants.COLOR + i, TransporterUtils.getColorIndex(inputColors[i]));
        }
        nbtTags.put(NBTConstants.COMPONENT_EJECTOR, ejectorNBT);
    }

    @Override
    public List<ISyncableData> getSpecificSyncableData() {
        List<ISyncableData> list = new ArrayList<>();
        list.add(SyncableBoolean.create(this::hasStrictInput, input -> strictInput = input));
        list.add(SyncableInt.create(() -> TransporterUtils.getColorIndex(outputColor), index -> outputColor = TransporterUtils.readColor(index)));
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int idx = i;
            list.add(SyncableInt.create(() -> TransporterUtils.getColorIndex(inputColors[idx]), index -> inputColors[idx] = TransporterUtils.readColor(index)));
        }
        return list;
    }

    //Computer related methods
    @ComputerMethod(nameOverride = "setStrictInput")
    private void computerSetStrictInput(boolean strict) throws ComputerException {
        tile.validateSecurityIsPublic();
        setStrictInput(strict);
    }

    private void validateInputSide(RelativeSide side) throws ComputerException {
        if (!isInputSideEnabled(side)) {
            throw new ComputerException("Side '%s' is disabled and can't be configured.", side);
        }
    }

    @ComputerMethod
    private void clearInputColor(RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateInputSide(side);
        setInputColor(side, null);
    }

    @ComputerMethod
    private void incrementInputColor(RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateInputSide(side);
        int ordinal = side.ordinal();
        inputColors[ordinal] = TransporterUtils.increment(inputColors[ordinal]);
        WorldUtils.saveChunk(tile);
    }

    @ComputerMethod
    private void decrementInputColor(RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateInputSide(side);
        int ordinal = side.ordinal();
        inputColors[ordinal] = TransporterUtils.decrement(inputColors[ordinal]);
        WorldUtils.saveChunk(tile);
    }

    @ComputerMethod(nameOverride = "setInputColor")
    private void computerSetInputColor(RelativeSide side, EnumColor color) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateInputSide(side);
        if (!TransporterUtils.colors.contains(color)) {
            throw new ComputerException("Color '%s' is not a supported transporter color.", color);
        }
        setInputColor(side, color);
    }

    @ComputerMethod
    private void clearOutputColor() throws ComputerException {
        tile.validateSecurityIsPublic();
        setOutputColor(null);
    }

    @ComputerMethod
    private void incrementOutputColor() throws ComputerException {
        tile.validateSecurityIsPublic();
        outputColor = TransporterUtils.increment(outputColor);
        WorldUtils.saveChunk(tile);
    }

    @ComputerMethod
    private void decrementOutputColor() throws ComputerException {
        tile.validateSecurityIsPublic();
        outputColor = TransporterUtils.decrement(outputColor);
        WorldUtils.saveChunk(tile);
    }

    @ComputerMethod(nameOverride = "setOutputColor")
    private void computerSetOutputColor(EnumColor color) throws ComputerException {
        tile.validateSecurityIsPublic();
        if (!TransporterUtils.colors.contains(color)) {
            throw new ComputerException("Color '%s' is not a supported transporter color.", color);
        }
        setOutputColor(color);
    }
    //End computer related methods

    private static class EjectTransitRequest extends TileTransitRequest {

        public Direction side;

        public EjectTransitRequest(BlockEntity tile, Direction side) {
            super(tile, side);
            this.side = side;
        }

        @Override
        public Direction getSide() {
            return side;
        }
    }
}