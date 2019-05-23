package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITileComponent;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class TileComponentEjector implements ITileComponent {

    private static final int GAS_OUTPUT = 256;
    private static final int FLUID_OUTPUT = 256;
    private TileEntityContainerBlock tileEntity;
    private boolean strictInput;
    private EnumColor outputColor;
    private EnumColor[] inputColors = new EnumColor[]{null, null, null, null, null, null};
    private int tickDelay = 0;
    private Map<TransmissionType, SideData> sideData = new EnumMap<>(TransmissionType.class);
    public Map<TransmissionType, int[]> trackers = new EnumMap<>(TransmissionType.class);

    public TileComponentEjector(TileEntityContainerBlock tile) {
        tileEntity = tile;
        tile.components.add(this);
    }

    public TileComponentEjector setOutputData(TransmissionType type, SideData data) {
        sideData.put(type, data);
        trackers.put(type, new int[data.availableSlots.length]);
        return this;
    }

    public void readFrom(TileComponentEjector ejector) {
        strictInput = ejector.strictInput;
        outputColor = ejector.outputColor;
        inputColors = ejector.inputColors;
        tickDelay = ejector.tickDelay;
        sideData = ejector.sideData;
    }

    private Set<EnumFacing> getTrackedOutputs(TransmissionType type, int index, Set<EnumFacing> dirs) {
        Set<EnumFacing> sides = EnumSet.noneOf(EnumFacing.class);
        int tracker = trackers.get(type)[index];
        for (int i = tracker + 1; i <= tracker + 6; i++) {
            EnumFacing side = EnumFacing.byIndex(i % 6);
            if (dirs.contains(side)) {
                sides.add(side);
            }
        }
        return sides;
    }

    @Override
    public void tick() {
        if (tickDelay == 0) {
            if (sideData.get(TransmissionType.ITEM) != null) {
                outputItems();
            }
        } else {
            tickDelay--;
        }
        if (!tileEntity.getWorld().isRemote) {
            if (sideData.get(TransmissionType.GAS) != null && getEjecting(TransmissionType.GAS)) {
                SideData data = sideData.get(TransmissionType.GAS);
                Set<EnumFacing> outputSides = getOutputSides(TransmissionType.GAS, data);
                if (((ITankManager) tileEntity).getTanks() != null) {
                    GasTank tank = (GasTank) ((ITankManager) tileEntity).getTanks()[data.availableSlots[0]];
                    if (tank.getStored() > 0) {
                        GasStack toEmit = tank.getGas().copy().withAmount(Math.min(GAS_OUTPUT, tank.getStored()));
                        int emit = GasUtils.emit(toEmit, tileEntity, outputSides);
                        tank.draw(emit, true);
                    }
                }
            }

            if (sideData.get(TransmissionType.FLUID) != null && getEjecting(TransmissionType.FLUID)) {
                SideData data = sideData.get(TransmissionType.FLUID);
                Set<EnumFacing> outputSides = getOutputSides(TransmissionType.FLUID, data);
                if (((ITankManager) tileEntity).getTanks() != null) {
                    FluidTank tank = (FluidTank) ((ITankManager) tileEntity).getTanks()[data.availableSlots[0]];
                    if (tank.getFluidAmount() > 0) {
                        FluidStack toEmit = PipeUtils.copy(tank.getFluid(), Math.min(FLUID_OUTPUT, tank.getFluidAmount()));
                        int emit = PipeUtils.emit(outputSides, toEmit, tileEntity);
                        tank.drain(emit, true);
                    }
                }
            }
        }
    }

    public Set<EnumFacing> getOutputSides(TransmissionType type, SideData data) {
        Set<EnumFacing> outputSides = EnumSet.noneOf(EnumFacing.class);
        TileComponentConfig config = ((ISideConfiguration) tileEntity).getConfig();
        SideConfig sideConfig = config.getConfig(type);
        List<SideData> outputs = config.getOutputs(type);
        EnumFacing[] facings = MekanismUtils.getBaseOrientations(tileEntity.facing);
        for (int i = 0; i < EnumFacing.VALUES.length; i++) {
            EnumFacing side = facings[i];
            if (sideConfig.get(side) == outputs.indexOf(data)) {
                outputSides.add(EnumFacing.VALUES[i]);
            }
        }
        return outputSides;
    }

    public void outputItems() {
        if (!getEjecting(TransmissionType.ITEM) || tileEntity.getWorld().isRemote) {
            return;
        }

        SideData data = sideData.get(TransmissionType.ITEM);
        Set<EnumFacing> outputSides = getOutputSides(TransmissionType.ITEM, data);
        for (int index = 0; index < data.availableSlots.length; index++) {
            int slotID = data.availableSlots[index];
            if (tileEntity.getStackInSlot(slotID).isEmpty()) {
                continue;
            }

            ItemStack stack = tileEntity.getStackInSlot(slotID);
            Coord4D tileCoord = Coord4D.get(tileEntity);
            Set<EnumFacing> outputs = getTrackedOutputs(TransmissionType.ITEM, index, outputSides);
            for (EnumFacing side : outputs) {
                TileEntity tile = tileCoord.offset(side).getTileEntity(tileEntity.getWorld());
                int prevCount = stack.getCount();

                ILogisticalTransporter capability = CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, side.getOpposite());
                TransitRequest transitRequest = TransitRequest.getFromStack(stack.copy());
                TransitResponse response;
                if (capability == null) {
                    response = InventoryUtils.putStackInInventory(tile, transitRequest, side, false);
                } else {
                    response = TransporterUtils.insert(tileEntity, capability, transitRequest, outputColor, true, 0);
                }
                if (!response.isEmpty()) {
                    stack.shrink(response.getSendingAmount());
                }

                if (stack.isEmpty() || prevCount != stack.getCount()) {
                    trackers.get(TransmissionType.ITEM)[index] = side.ordinal();
                }
                if (stack.isEmpty()) {
                    break;
                }
            }

            tileEntity.setInventorySlotContents(slotID, stack);
            tileEntity.markDirty();
        }
        tickDelay = 20;
    }

    public boolean hasStrictInput() {
        return strictInput;
    }

    public void setStrictInput(boolean strict) {
        strictInput = strict;
        MekanismUtils.saveChunk(tileEntity);
    }

    public EnumColor getOutputColor() {
        return outputColor;
    }

    public void setOutputColor(EnumColor color) {
        outputColor = color;
        MekanismUtils.saveChunk(tileEntity);
    }

    public void setInputColor(EnumFacing side, EnumColor color) {
        inputColors[side.ordinal()] = color;
        MekanismUtils.saveChunk(tileEntity);
    }

    public EnumColor getInputColor(EnumFacing side) {
        return inputColors[side.ordinal()];
    }

    @Override
    public void read(NBTTagCompound nbtTags) {
        strictInput = nbtTags.getBoolean("strictInput");
        if (nbtTags.hasKey("ejectColor")) {
            outputColor = readColor(nbtTags.getInteger("ejectColor"));
        }
        for (Entry<TransmissionType, SideData> entry : sideData.entrySet()) {
            TransmissionType type = entry.getKey();
            SideData data = entry.getValue();
            for (int i = 0; i < data.availableSlots.length; i++) {
                trackers.get(type)[i] = nbtTags.getInteger("tracker" + type.getTransmission() + i);
            }
        }
        for (int i = 0; i < 6; i++) {
            if (nbtTags.hasKey("inputColors" + i)) {
                inputColors[i] = readColor(nbtTags.getInteger("inputColors" + i));
            }
        }
    }

    @Override
    public void read(ByteBuf dataStream) {
        strictInput = dataStream.readBoolean();
        outputColor = readColor(dataStream.readInt());
        for (int i = 0; i < 6; i++) {
            inputColors[i] = readColor(dataStream.readInt());
        }
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        nbtTags.setBoolean("strictInput", strictInput);
        if (outputColor != null) {
            nbtTags.setInteger("ejectColor", getColorIndex(outputColor));
        }
        for (Entry<TransmissionType, SideData> entry : sideData.entrySet()) {
            TransmissionType type = entry.getKey();
            SideData data = entry.getValue();
            for (int i = 0; i < data.availableSlots.length; i++) {
                nbtTags.setInteger("tracker" + type.getTransmission() + i, trackers.get(type)[i]);
            }
        }
        for (int i = 0; i < 6; i++) {
            nbtTags.setInteger("inputColors" + i, getColorIndex(inputColors[i]));
        }
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

    private boolean getEjecting(TransmissionType type) {
        return ((ISideConfiguration) tileEntity).getConfig().isEjecting(type);
    }
}