package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.lib.inventory.TileTransitRequest;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.InfusionSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.PigmentSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.SlurrySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ILogisticalTransporter;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentEjector implements ITileComponent, ISpecificContainerTracker {

    //TODO: Figure out why these limits for output rates are here/if there should be an upgrade that modifies the output rates
    //TODO - V10: Make these output rates configurable
    private static final long CHEMICAL_OUTPUT = 256;
    private static final int FLUID_OUTPUT = 256;
    private final TileEntityMekanism tile;
    private final Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
    private final EnumColor[] inputColors = new EnumColor[]{null, null, null, null, null, null};
    private boolean strictInput;
    private EnumColor outputColor;
    private int tickDelay = 0;

    public TileComponentEjector(TileEntityMekanism tile) {
        this.tile = tile;
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

    @Override
    public void tick() {
        if (!tile.isRemote()) {
            if (tickDelay == 0) {
                outputItems();
            } else {
                tickDelay--;
            }
            eject(TransmissionType.GAS);
            eject(TransmissionType.INFUSION);
            eject(TransmissionType.SLURRY);
            eject(TransmissionType.PIGMENT);
            eject(TransmissionType.FLUID);
        }
    }

    private void eject(TransmissionType type) {
        ConfigInfo info = configInfo.get(type);
        if (info != null && info.isEjecting()) {
            for (DataType dataType : info.getSupportedDataTypes()) {
                if (dataType.canOutput()) {
                    ISlotInfo slotInfo = info.getSlotInfo(dataType);
                    if (slotInfo != null) {
                        Set<Direction> outputSides = info.getSidesForData(dataType);
                        //TODO - V10: Properly eject for other chemical transmission types
                        if (type == TransmissionType.GAS && slotInfo instanceof GasSlotInfo) {
                            ((GasSlotInfo) slotInfo).getTanks().forEach(tank -> GasUtils.emit(outputSides, tank, tile, CHEMICAL_OUTPUT));
                        } else if (type == TransmissionType.INFUSION && slotInfo instanceof InfusionSlotInfo) {
                            //((InfusionSlotInfo) slotInfo).getTanks().forEach(tank -> GasUtils.emit(outputSides, tank, tile, CHEMICAL_OUTPUT));
                        } else if (type == TransmissionType.PIGMENT && slotInfo instanceof PigmentSlotInfo) {
                            //((PigmentSlotInfo) slotInfo).getTanks().forEach(tank -> GasUtils.emit(outputSides, tank, tile, CHEMICAL_OUTPUT));
                        } else if (type == TransmissionType.SLURRY && slotInfo instanceof SlurrySlotInfo) {
                            //((SlurrySlotInfo) slotInfo).getTanks().forEach(tank -> GasUtils.emit(outputSides, tank, tile, CHEMICAL_OUTPUT));
                        } else if (type == TransmissionType.FLUID && slotInfo instanceof FluidSlotInfo) {
                            ((FluidSlotInfo) slotInfo).getTanks().forEach(tank -> FluidUtils.emit(outputSides, tank, tile, FLUID_OUTPUT));
                        }
                    }
                }
            }
        }
    }

    private void outputItems() {
        ConfigInfo info = configInfo.get(TransmissionType.ITEM);
        if (info == null || !info.isEjecting()) {
            return;
        }
        for (DataType dataType : info.getSupportedDataTypes()) {
            if (!dataType.canOutput()) {
                continue;
            }
            ISlotInfo slotInfo = info.getSlotInfo(dataType);
            if (!(slotInfo instanceof InventorySlotInfo)) {
                //We need it to be inventory slot info
                return;
            }
            Set<Direction> outputs = info.getSidesForData(dataType);
            if (!outputs.isEmpty()) {
                TransitRequest ejectMap = getEjectItemMap((InventorySlotInfo) slotInfo, outputs.iterator().next());
                if (!ejectMap.isEmpty()) {
                    for (Direction side : outputs) {
                        TileEntity tile = MekanismUtils.getTileEntity(this.tile.getWorld(), this.tile.getPos().offset(side));
                        if (tile == null) {
                            //If the spot is not loaded just skip trying to eject to it
                            continue;
                        }
                        TransitResponse response;
                        Optional<ILogisticalTransporter> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, side.getOpposite()));
                        if (capability.isPresent()) {
                            response = capability.get().insert(this.tile, ejectMap, outputColor, true, 0);
                        } else {
                            response = ejectMap.addToInventory(tile, side, false);
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

        tickDelay = 10;
    }

    private TransitRequest getEjectItemMap(InventorySlotInfo slotInfo, Direction side) {
        TileTransitRequest request = new TileTransitRequest(tile, side);
        List<IInventorySlot> slots = slotInfo.getSlots();
        // shuffle the order we look at our slots to avoid ejection patterns
        List<IInventorySlot> shuffled = new ArrayList<>(slots);
        Collections.shuffle(shuffled);
        for (int index = 0; index < shuffled.size(); index++) {
            IInventorySlot slot = shuffled.get(index);
            //Note: We are using EXTERNAL as that is what we actually end up using when performing the extraction in the end
            ItemStack simulatedExtraction = slot.extractItem(slot.getCount(), Action.SIMULATE, AutomationType.EXTERNAL);
            if (!simulatedExtraction.isEmpty()) {
                request.addItem(simulatedExtraction, index);
            }
        }
        return request;
    }

    public boolean hasStrictInput() {
        return strictInput;
    }

    public void setStrictInput(boolean strict) {
        strictInput = strict;
        MekanismUtils.saveChunk(tile);
    }

    public EnumColor getOutputColor() {
        return outputColor;
    }

    public void setOutputColor(EnumColor color) {
        outputColor = color;
        MekanismUtils.saveChunk(tile);
    }

    public void setInputColor(RelativeSide side, EnumColor color) {
        inputColors[side.ordinal()] = color;
        MekanismUtils.saveChunk(tile);
    }

    public EnumColor getInputColor(RelativeSide side) {
        return inputColors[side.ordinal()];
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.contains(NBTConstants.COMPONENT_EJECTOR, NBT.TAG_COMPOUND)) {
            CompoundNBT ejectorNBT = nbtTags.getCompound(NBTConstants.COMPONENT_EJECTOR);
            strictInput = ejectorNBT.getBoolean(NBTConstants.STRICT_INPUT);
            NBTUtils.setEnumIfPresent(ejectorNBT, NBTConstants.COLOR, TransporterUtils::readColor, color -> outputColor = color);
            //Input colors
            for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
                int index = i;
                NBTUtils.setEnumIfPresent(ejectorNBT, NBTConstants.COLOR + index, TransporterUtils::readColor, color -> inputColors[index] = color);
            }
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        CompoundNBT ejectorNBT = new CompoundNBT();
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
    public void trackForMainContainer(MekanismContainer container) {
    }

    @Override
    public void addToUpdateTag(CompoundNBT updateTag) {
    }

    @Override
    public void readFromUpdateTag(CompoundNBT updateTag) {
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
}