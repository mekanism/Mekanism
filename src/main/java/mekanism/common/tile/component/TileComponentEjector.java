package mekanism.common.tile.component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITileComponent;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileComponentEjector implements ITileComponent {

    private static final int GAS_OUTPUT = 256;
    private static final int FLUID_OUTPUT = 256;
    private TileEntityMekanism tileEntity;
    private boolean strictInput;
    private EnumColor outputColor;
    private EnumColor[] inputColors = new EnumColor[]{null, null, null, null, null, null};
    private int tickDelay = 0;
    private Map<TransmissionType, SideData> sideData = new EnumMap<>(TransmissionType.class);

    public TileComponentEjector(TileEntityMekanism tile) {
        tileEntity = tile;
        tile.addComponent(this);
    }

    public TileComponentEjector setOutputData(TransmissionType type, SideData data) {
        sideData.put(type, data);
        return this;
    }

    public void readFrom(TileComponentEjector ejector) {
        strictInput = ejector.strictInput;
        outputColor = ejector.outputColor;
        inputColors = ejector.inputColors;
        tickDelay = ejector.tickDelay;
        sideData = ejector.sideData;
    }

    @Override
    public void tick() {
        if (!tileEntity.getWorld().isRemote) {
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
        SideData data = sideData.get(type);
        if (data != null && getEjecting(type)) {
            ITankManager tankManager = (ITankManager) this.tileEntity;
            if (tankManager.getTanks() != null) {
                Set<Direction> outputSides = getOutputSides(type, data);
                if (type == TransmissionType.GAS) {
                    ejectGas(outputSides, (GasTank) tankManager.getTanks()[data.availableSlots[0]]);
                } else if (type == TransmissionType.FLUID) {
                    ejectFluid(outputSides, (FluidTank) tankManager.getTanks()[data.availableSlots[0]]);
                }
            }
        }
    }

    private void ejectGas(Set<Direction> outputSides, GasTank tank) {
        if (tank.getGas() != null && tank.getStored() > 0) {
            GasStack toEmit = tank.getGas().copy().withAmount(Math.min(GAS_OUTPUT, tank.getStored()));
            int emit = GasUtils.emit(toEmit, tileEntity, outputSides);
            tank.draw(emit, true);
        }
    }

    private void ejectFluid(Set<Direction> outputSides, FluidTank tank) {
        if (!tank.getFluid().isEmpty() && tank.getFluidAmount() > 0) {
            FluidStack toEmit = PipeUtils.copy(tank.getFluid(), Math.min(FLUID_OUTPUT, tank.getFluidAmount()));
            int emit = PipeUtils.emit(outputSides, toEmit, tileEntity);
            tank.drain(emit, FluidAction.EXECUTE);
        }
    }

    public Set<Direction> getOutputSides(TransmissionType type, SideData data) {
        Set<Direction> outputSides = EnumSet.noneOf(Direction.class);
        TileComponentConfig config = ((ISideConfiguration) tileEntity).getConfig();
        SideConfig sideConfig = config.getConfig(type);
        List<SideData> outputs = config.getOutputs(type);
        Direction[] facings = MekanismUtils.getBaseOrientations(tileEntity.getDirection());
        for (int i = 0; i < Direction.values().length; i++) {
            Direction side = facings[i];
            if (sideConfig.get(side) == outputs.indexOf(data)) {
                outputSides.add(Direction.values()[i]);
            }
        }
        return outputSides;
    }

    private void outputItems() {
        SideData data = sideData.get(TransmissionType.ITEM);
        if (data == null || !getEjecting(TransmissionType.ITEM)) {
            return;
        }
        Set<Direction> outputs = getOutputSides(TransmissionType.ITEM, data);
        TransitRequest ejectMap = null;
        for (Direction side : outputs) {
            if (ejectMap == null) {
                ejectMap = getEjectItemMap(data);
                if (ejectMap.isEmpty()) {
                    break;
                }
            }
            TileEntity tile = MekanismUtils.getTileEntity(tileEntity.getWorld(), tileEntity.getPos().offset(side));
            if (tile == null) {
                //If the spot is not loaded just skip trying to eject to it
                continue;
            }
            TransitRequest finalEjectMap = ejectMap;
            TransitResponse response = CapabilityUtils.getCapabilityHelper(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, side.getOpposite()).getIfPresentElseDo(
                  transporter -> TransporterUtils.insert(tileEntity, transporter, finalEjectMap, outputColor, true, 0),
                  () -> InventoryUtils.putStackInInventory(tile, finalEjectMap, side, false)
            );
            if (!response.isEmpty()) {
                response.getInvStack(tileEntity, side).use();
                //Set map to null so next loop recalculates the eject map so that all sides get a chance to be ejected to
                // assuming that there is still any left
                //TODO: Eventually make some way to just directly update the TransitRequest with remaining parts
                ejectMap = null;
            }
        }
        tickDelay = 10;
    }

    private TransitRequest getEjectItemMap(SideData data) {
        TransitRequest request = new TransitRequest();
        for (int index = 0; index < data.availableSlots.length; index++) {
            int slotID = data.availableSlots[index];
            ItemStack stack = tileEntity.getStackInSlot(slotID);
            if (!stack.isEmpty()) {
                request.addItem(stack, index);
            }
        }
        return request;
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

    public void setInputColor(Direction side, EnumColor color) {
        inputColors[side.ordinal()] = color;
        MekanismUtils.saveChunk(tileEntity);
    }

    public EnumColor getInputColor(Direction side) {
        return inputColors[side.ordinal()];
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        strictInput = nbtTags.getBoolean("strictInput");
        if (nbtTags.contains("ejectColor")) {
            outputColor = readColor(nbtTags.getInt("ejectColor"));
        }
        for (int i = 0; i < 6; i++) {
            if (nbtTags.contains("inputColors" + i)) {
                inputColors[i] = readColor(nbtTags.getInt("inputColors" + i));
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
        nbtTags.putBoolean("strictInput", strictInput);
        if (outputColor != null) {
            nbtTags.putInt("ejectColor", getColorIndex(outputColor));
        }
        for (int i = 0; i < 6; i++) {
            nbtTags.putInt("inputColors" + i, getColorIndex(inputColors[i]));
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