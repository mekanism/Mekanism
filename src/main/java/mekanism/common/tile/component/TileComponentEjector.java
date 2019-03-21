package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITileComponent;
import mekanism.api.TileNetworkList;
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

    public static final int GAS_OUTPUT = 256;
    public static final int FLUID_OUTPUT = 256;
    public TileEntityContainerBlock tileEntity;
    public boolean strictInput;
    public EnumColor outputColor;
    public EnumColor[] inputColors = new EnumColor[]{null, null, null, null, null, null};
    public int tickDelay = 0;
    public Map<TransmissionType, SideData> sideData = new HashMap<>();
    public Map<TransmissionType, int[]> trackers = new HashMap<>();

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

    private List<EnumFacing> getTrackedOutputs(TransmissionType type, int index, List<EnumFacing> dirs) {
        List<EnumFacing> sides = new ArrayList<>();

        for (int i = trackers.get(type)[index] + 1; i <= trackers.get(type)[index] + 6; i++) {
            for (EnumFacing side : dirs) {
                if (EnumFacing.byIndex(i % 6) == side) {
                    sides.add(side);
                }
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
                List<EnumFacing> outputSides = getOutputSides(TransmissionType.GAS, data);

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
                List<EnumFacing> outputSides = getOutputSides(TransmissionType.FLUID, data);

                if (((ITankManager) tileEntity).getTanks() != null) {
                    FluidTank tank = (FluidTank) ((ITankManager) tileEntity).getTanks()[data.availableSlots[0]];

                    if (tank.getFluidAmount() > 0) {
                        FluidStack toEmit = PipeUtils
                              .copy(tank.getFluid(), Math.min(FLUID_OUTPUT, tank.getFluidAmount()));
                        int emit = PipeUtils.emit(outputSides, toEmit, tileEntity);
                        tank.drain(emit, true);
                    }
                }
            }
        }
    }

    public List<EnumFacing> getOutputSides(TransmissionType type, SideData data) {
        List<EnumFacing> outputSides = new ArrayList<>();
        ISideConfiguration configurable = (ISideConfiguration) tileEntity;

        SideConfig sideConfig = configurable.getConfig().getConfig(type);
        ArrayList<SideData> outputs = configurable.getConfig().getOutputs(type);

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
        List<EnumFacing> outputSides = getOutputSides(TransmissionType.ITEM, data);

        for (int index = 0; index < sideData.get(TransmissionType.ITEM).availableSlots.length; index++) {
            int slotID = sideData.get(TransmissionType.ITEM).availableSlots[index];

            if (tileEntity.getStackInSlot(slotID).isEmpty()) {
                continue;
            }

            ItemStack stack = tileEntity.getStackInSlot(slotID);
            List<EnumFacing> outputs = getTrackedOutputs(TransmissionType.ITEM, index, outputSides);

            for (EnumFacing side : outputs) {
                TileEntity tile = Coord4D.get(tileEntity).offset(side).getTileEntity(tileEntity.getWorld());
                ItemStack prev = stack.copy();

                if (CapabilityUtils
                      .hasCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, side.getOpposite())) {
                    TransitResponse response = TransporterUtils.insert(tileEntity, CapabilityUtils
                                .getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, side.getOpposite()),
                          TransitRequest.getFromStack(stack.copy()), outputColor, true, 0);

                    if (!response.isEmpty()) {
                        stack.shrink(response.stack.getCount());
                    }
                } else {
                    TransitResponse response = InventoryUtils
                          .putStackInInventory(tile, TransitRequest.getFromStack(stack.copy()), side, false);

                    if (!response.isEmpty()) {
                        stack.shrink(response.stack.getCount());
                    }
                }

                if (stack.isEmpty() || prev.getCount() != stack.getCount()) {
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
            outputColor = TransporterUtils.colors.get(nbtTags.getInteger("ejectColor"));
        }

        for (TransmissionType type : sideData.keySet()) {
            for (int i = 0; i < sideData.get(type).availableSlots.length; i++) {
                trackers.get(type)[i] = nbtTags.getInteger("tracker" + type.getTransmission() + i);
            }
        }

        for (int i = 0; i < 6; i++) {
            if (nbtTags.hasKey("inputColors" + i)) {
                int inC = nbtTags.getInteger("inputColors" + i);

                if (inC != -1) {
                    inputColors[i] = TransporterUtils.colors.get(inC);
                } else {
                    inputColors[i] = null;
                }
            }
        }
    }

    @Override
    public void read(ByteBuf dataStream) {
        strictInput = dataStream.readBoolean();

        int c = dataStream.readInt();

        if (c != -1) {
            outputColor = TransporterUtils.colors.get(c);
        } else {
            outputColor = null;
        }

        for (int i = 0; i < 6; i++) {
            int inC = dataStream.readInt();

            if (inC != -1) {
                inputColors[i] = TransporterUtils.colors.get(inC);
            } else {
                inputColors[i] = null;
            }
        }
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        nbtTags.setBoolean("strictInput", strictInput);

        if (outputColor != null) {
            nbtTags.setInteger("ejectColor", TransporterUtils.colors.indexOf(outputColor));
        }

        for (TransmissionType type : sideData.keySet()) {
            for (int i = 0; i < sideData.get(type).availableSlots.length; i++) {
                nbtTags.setInteger("tracker" + type.getTransmission() + i, trackers.get(type)[i]);
            }
        }

        for (int i = 0; i < 6; i++) {
            if (inputColors[i] == null) {
                nbtTags.setInteger("inputColors" + i, -1);
            } else {
                nbtTags.setInteger("inputColors" + i, TransporterUtils.colors.indexOf(inputColors[i]));
            }
        }
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(strictInput);

        if (outputColor != null) {
            data.add(TransporterUtils.colors.indexOf(outputColor));
        } else {
            data.add(-1);
        }

        for (int i = 0; i < 6; i++) {
            if (inputColors[i] == null) {
                data.add(-1);
            } else {
                data.add(TransporterUtils.colors.indexOf(inputColors[i]));
            }
        }
    }

    @Override
    public void invalidate() {
    }

    private boolean getEjecting(TransmissionType type) {
        return ((ISideConfiguration) tileEntity).getConfig().isEjecting(type);
    }
}
