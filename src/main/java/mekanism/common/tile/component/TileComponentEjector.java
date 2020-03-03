package mekanism.common.tile.component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITileComponent;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class TileComponentEjector implements ITileComponent {

    //TODO: Figure out why these limits for output rates are here/if there should be an upgrade that modifies the output rates
    private static final int GAS_OUTPUT = 256;
    private static final int FLUID_OUTPUT = 256;
    private TileEntityMekanism tile;
    private boolean strictInput;
    private EnumColor outputColor;
    private EnumColor[] inputColors = new EnumColor[]{null, null, null, null, null, null};
    private int tickDelay = 0;
    private Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);

    public TileComponentEjector(TileEntityMekanism tile) {
        this.tile = tile;
        tile.addComponent(this);
    }

    public TileComponentEjector setOutputData(TransmissionType type, @Nullable ConfigInfo info) {
        if (info != null) {
            configInfo.put(type, info);
        }
        return this;
    }

    public void readFrom(TileComponentEjector ejector) {
        strictInput = ejector.strictInput;
        outputColor = ejector.outputColor;
        inputColors = ejector.inputColors;
        tickDelay = ejector.tickDelay;
        configInfo = ejector.configInfo;
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
            eject(TransmissionType.FLUID);
        }
    }

    private void eject(TransmissionType type) {
        ConfigInfo info = configInfo.get(type);
        if (info != null && info.isEjecting()) {
            ISlotInfo slotInfo = info.getSlotInfo(DataType.OUTPUT);
            if (slotInfo != null) {
                Set<Direction> outputSides = info.getSidesForData(DataType.OUTPUT);
                if (type == TransmissionType.GAS && slotInfo instanceof GasSlotInfo) {
                    ((GasSlotInfo) slotInfo).getTanks().forEach(tank -> ejectGas(outputSides, tank));
                } else if (type == TransmissionType.FLUID && slotInfo instanceof FluidSlotInfo) {
                    ((FluidSlotInfo) slotInfo).getTanks().forEach(tank -> ejectFluid(outputSides, tank));
                }
            }
        }
    }

    private void ejectGas(Set<Direction> outputSides, IChemicalTank<Gas, GasStack> tank) {
        if (!tank.isEmpty()) {
            GasStack toEmit = new GasStack(tank.getStack(), Math.min(GAS_OUTPUT, tank.getStored()));
            //Shrink the stack by the amount we are able to emit
            tank.shrinkStack(GasUtils.emit(toEmit, tile, outputSides), Action.EXECUTE);
        }
    }

    private void ejectFluid(Set<Direction> outputSides, IExtendedFluidTank tank) {
        if (!tank.isEmpty()) {
            FluidStack toEmit = new FluidStack(tank.getFluid(), Math.min(FLUID_OUTPUT, tank.getFluidAmount()));
            tank.extract(PipeUtils.emit(outputSides, toEmit, tile), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    private void outputItems() {
        ConfigInfo info = configInfo.get(TransmissionType.ITEM);
        if (info == null || !info.isEjecting()) {
            return;
        }
        //TODO: Do we want to check ejecting for EACH data type that is there, if the slot allows outputting, or do we just want to then check DataType.OUTPUT
        // For now just doing the specific output type because it makes more sense
        Set<Direction> outputs = info.getSidesForData(DataType.OUTPUT);
        ISlotInfo slotInfo = info.getSlotInfo(DataType.OUTPUT);
        if (!(slotInfo instanceof InventorySlotInfo)) {
            //We need it to be inventory slot info
            return;
        }
        TransitRequest ejectMap = null;
        for (Direction side : outputs) {
            if (ejectMap == null) {
                ejectMap = getEjectItemMap((InventorySlotInfo) slotInfo);
                if (ejectMap.isEmpty()) {
                    break;
                }
            }
            TileEntity tile = MekanismUtils.getTileEntity(this.tile.getWorld(), this.tile.getPos().offset(side));
            if (tile == null) {
                //If the spot is not loaded just skip trying to eject to it
                continue;
            }
            TransitRequest finalEjectMap = ejectMap;
            TransitResponse response;
            Optional<ILogisticalTransporter> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, side.getOpposite()));
            if (capability.isPresent()) {
                response = capability.get().insert(this.tile, finalEjectMap, outputColor, true, 0);
            } else {
                response = InventoryUtils.putStackInInventory(tile, finalEjectMap, side, false);
            }
            if (!response.isEmpty()) {
                response.getInvStack(this.tile, side).use();
                //Set map to null so next loop recalculates the eject map so that all sides get a chance to be ejected to
                // assuming that there is still any left
                //TODO: Eventually make some way to just directly update the TransitRequest with remaining parts
                ejectMap = null;
            }
        }
        tickDelay = 10;
    }

    private TransitRequest getEjectItemMap(InventorySlotInfo slotInfo) {
        TransitRequest request = new TransitRequest();
        List<IInventorySlot> slots = slotInfo.getSlots();
        for (int index = 0; index < slots.size(); index++) {
            IInventorySlot slot = slots.get(index);
            if (!slot.isEmpty()) {
                //TODO: verify the stack can't be modified or give it a copy
                request.addItem(slot.getStack(), index);
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
        if (nbtTags.contains("componentEjector", NBT.TAG_COMPOUND)) {
            CompoundNBT ejectorNBT = nbtTags.getCompound("componentEjector");
            strictInput = ejectorNBT.getBoolean("strictInput");
            if (ejectorNBT.contains("ejectColor", NBT.TAG_INT)) {
                outputColor = readColor(ejectorNBT.getInt("ejectColor"));
            }
            for (int i = 0; i < 6; i++) {
                if (ejectorNBT.contains("inputColors" + i, NBT.TAG_INT)) {
                    inputColors[i] = readColor(ejectorNBT.getInt("inputColors" + i));
                }
            }
        }
    }

    @Override
    public void read(PacketBuffer dataStream) {
        strictInput = dataStream.readBoolean();
        outputColor = readColor(dataStream.readInt());
        for (int i = 0; i < 6; i++) {
            inputColors[i] = readColor(dataStream.readInt());
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        CompoundNBT ejectorNBT = new CompoundNBT();
        ejectorNBT.putBoolean("strictInput", strictInput);
        if (outputColor != null) {
            ejectorNBT.putInt("ejectColor", getColorIndex(outputColor));
        }
        for (int i = 0; i < 6; i++) {
            ejectorNBT.putInt("inputColors" + i, getColorIndex(inputColors[i]));
        }
        nbtTags.put("componentEjector", ejectorNBT);
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(strictInput);
        data.add(getColorIndex(outputColor));
        for (int i = 0; i < 6; i++) {
            data.add(getColorIndex(inputColors[i]));
        }
    }

    private EnumColor readColor(int inputColor) {
        if (inputColor == -1) {
            return null;
        }
        return TransporterUtils.colors.get(inputColor);
    }

    private int getColorIndex(EnumColor color) {
        if (color == null) {
            return -1;
        }
        return TransporterUtils.colors.indexOf(color);
    }

    @Override
    public void invalidate() {
    }
}