package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.SideData.IOState;
import mekanism.common.base.ITileComponent;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileComponentConfig implements ITileComponent {

    public static SideData EMPTY = new SideData("Empty", EnumColor.BLACK, InventoryUtils.EMPTY);

    public TileEntityMekanism tileEntity;
    private List<TransmissionType> transmissions = new ArrayList<>();
    private Map<TransmissionType, SideConfig> sideConfigs = new EnumMap<>(TransmissionType.class);
    private Map<TransmissionType, ArrayList<SideData>> sideOutputs = new EnumMap<>(TransmissionType.class);
    private Map<TransmissionType, Boolean> ejecting = new EnumMap<>(TransmissionType.class);
    private Map<TransmissionType, Boolean> canEject = new EnumMap<>(TransmissionType.class);

    public TileComponentConfig(TileEntityMekanism tile, TransmissionType... types) {
        tileEntity = tile;
        for (TransmissionType type : types) {
            addSupported(type);
        }
        tile.addComponent(this);
    }

    public List<TransmissionType> getTransmissions() {
        return transmissions;
    }

    public void readFrom(TileComponentConfig config) {
        sideConfigs = config.sideConfigs;
        ejecting = config.ejecting;
        canEject = config.canEject;
        transmissions = config.transmissions;
    }

    public void addSupported(TransmissionType type) {
        if (!transmissions.contains(type)) {
            transmissions.add(type);
        }
        sideOutputs.put(type, new ArrayList<>());
        ejecting.put(type, false);
        canEject.put(type, true);
    }

    public Set<Direction> getSidesForData(TransmissionType type, Direction facing, int dataIndex) {
        Set<Direction> ret = EnumSet.noneOf(Direction.class);
        SideConfig config = getConfig(type);
        Direction[] translatedFacings = MekanismUtils.getBaseOrientations(facing);

        for (Direction sideToCheck : EnumUtils.DIRECTIONS) {
            if (config.get(translatedFacings[sideToCheck.ordinal()]) == dataIndex) {
                ret.add(sideToCheck);
            }
        }
        return ret;
    }

    public boolean hasSideForData(TransmissionType type, Direction facing, int dataIndex, Direction sideToTest) {
        if (sideToTest == null) {
            return false;
        }
        Direction[] translatedFacings = MekanismUtils.getBaseOrientations(facing);
        return getConfig(type).get(translatedFacings[sideToTest.ordinal()]) == dataIndex;
    }

    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side, Direction tileDirection) {
        TransmissionType type = null;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            type = TransmissionType.ITEM;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            type = TransmissionType.GAS;
        } else if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            type = TransmissionType.HEAT;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            type = TransmissionType.FLUID;
        }
        //Energy is handled by the TileEntityElectricBlock anyways in the super clauses so no need to bother with it
        if (type != null) {
            return supports(type) && hasSideForData(type, tileDirection, 0, side);
        }
        return false;
    }

    public void setCanEject(TransmissionType type, boolean eject) {
        canEject.put(type, eject);
    }

    public boolean canEject(TransmissionType type) {
        return canEject.get(type);
    }

    public void fillConfig(TransmissionType type, int data) {
        byte sideData = (byte) data;
        setConfig(type, sideData, sideData, sideData, sideData, sideData, sideData);
    }

    public void setIOConfig(TransmissionType type) {
        addOutput(type, new SideData("None", EnumColor.GRAY, IOState.OFF));
        addOutput(type, new SideData("Input", EnumColor.DARK_GREEN, IOState.INPUT));
        addOutput(type, new SideData("Output", EnumColor.DARK_RED, IOState.OUTPUT));
        setConfig(type, new byte[]{1, 1, 2, 1, 1, 1});
    }

    public void setInputConfig(TransmissionType type) {
        addOutput(type, new SideData("None", EnumColor.GRAY, IOState.OFF));
        addOutput(type, new SideData("Input", EnumColor.DARK_GREEN, IOState.INPUT));
        fillConfig(type, 1);
        setCanEject(type, false);
    }

    public void setConfig(TransmissionType type, byte[] config) {
        assert config.length == EnumUtils.DIRECTIONS.length;
        setConfig(type, config[0], config[1], config[2], config[3], config[4], config[5]);
    }

    public void setConfig(TransmissionType type, byte d, byte u, byte n, byte s, byte w, byte e) {
        sideConfigs.put(type, new SideConfig(d, u, n, s, w, e));
    }

    public void addOutput(TransmissionType type, SideData data) {
        sideOutputs.get(type).add(data);
    }

    public ArrayList<SideData> getOutputs(TransmissionType type) {
        return sideOutputs.get(type);
    }

    public SideConfig getConfig(TransmissionType type) {
        return sideConfigs.get(type);
    }

    public SideData getOutput(TransmissionType type, Direction side, Direction facing) {
        if (side == null) {
            return EMPTY;
        }
        return getOutput(type, MekanismUtils.getBaseOrientation(side, facing));
    }

    public SideData getOutput(TransmissionType type, Direction side) {
        if (side == null) {
            return EMPTY;
        }

        SideConfig sideConfig = getConfig(type);
        int index = sideConfig.get(side);

        if (index == -1) {
            return EMPTY;
        } else if (index > getOutputs(type).size() - 1) {
            sideConfig.set(side, (byte) 0);
            index = 0;
        }
        return getOutputs(type).get(index);
    }

    public boolean supports(TransmissionType type) {
        return transmissions.contains(type);
    }

    @Override
    public void tick() {
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.getBoolean("sideDataStored")) {
            for (TransmissionType type : transmissions) {
                if (nbtTags.getByteArray("config" + type.ordinal()).length > 0) {
                    sideConfigs.put(type, new SideConfig(nbtTags.getByteArray("config" + type.ordinal())));
                    ejecting.put(type, nbtTags.getBoolean("ejecting" + type.ordinal()));
                }
            }
        }
    }

    @Override
    public void read(PacketBuffer dataStream) {
        transmissions.clear();

        int amount = dataStream.readInt();

        for (int i = 0; i < amount; i++) {
            transmissions.add(dataStream.readEnumValue(TransmissionType.class));
        }

        for (TransmissionType type : transmissions) {
            byte[] array = new byte[6];
            dataStream.readBytes(array);
            sideConfigs.put(type, new SideConfig(array));
            ejecting.put(type, dataStream.readBoolean());
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        for (TransmissionType type : transmissions) {
            nbtTags.putByteArray("config" + type.ordinal(), sideConfigs.get(type).asByteArray());
            nbtTags.putBoolean("ejecting" + type.ordinal(), ejecting.get(type));
        }
        nbtTags.putBoolean("sideDataStored", true);
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(transmissions.size());

        data.addAll(transmissions);
        for (TransmissionType type : transmissions) {
            data.add(sideConfigs.get(type).asByteArray());
            data.add(ejecting.get(type));
        }
    }

    @Override
    public void invalidate() {
    }

    public boolean isEjecting(TransmissionType type) {
        return ejecting.get(type);
    }

    public void setEjecting(TransmissionType type, boolean eject) {
        ejecting.put(type, eject);
        MekanismUtils.saveChunk(tileEntity);
    }
}