package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.ITileComponent;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

//TODO: Figure this out
public class TileComponentConfig implements ITileComponent {

    public TileEntityMekanism tileEntity;
    private Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);

    public TileComponentConfig(TileEntityMekanism tile, TransmissionType... types) {
        tileEntity = tile;
        for (TransmissionType type : types) {
            addSupported(type);
        }
        tile.addComponent(this);
    }

    private RelativeSide getSide(Direction direction) {
        //TODO: Instead of getDirection this should use ISideConfiguration#getOrientation
        return RelativeSide.fromDirections(tileEntity.getDirection(), direction);
    }

    public List<TransmissionType> getTransmissions() {
        //TODO: Do this better
        return new ArrayList<>(configInfo.keySet());
    }

    public void addSupported(TransmissionType type) {
        if (!configInfo.containsKey(type)) {
            //TODO: ISideConfiguration#getOrientation?
            configInfo.put(type, new ConfigInfo(() -> tileEntity.getDirection()));
        }
    }

    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
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
        //TODO: We actually might as well deal with energy here?
        if (type != null && supports(type)) {
            ISlotInfo slotInfo = getSlotInfo(type, side);
            return slotInfo != null && slotInfo.isEnabled();
        }
        return false;
    }

    public boolean canEject(TransmissionType type) {
        ConfigInfo info = getConfig(type);
        return info != null && info.canEject();
    }

    @Deprecated
    public ArrayList<SideData> getOutputs(TransmissionType type) {

        return null;
    }

    @Nullable
    public ConfigInfo getConfig(TransmissionType type) {
        return configInfo.get(type);
    }

    @Nullable
    public DataType getDataType(TransmissionType type, Direction direction) {
        return getDataType(type, getSide(direction));
    }

    @Nullable
    public DataType getDataType(TransmissionType type, RelativeSide side) {
        ConfigInfo info = getConfig(type);
        if (info != null) {
            return info.getDataType(side);
        }
        return null;
    }

    @Nullable
    public ISlotInfo getSlotInfo(TransmissionType type, Direction direction) {
        if (direction == null) {
            return null;
        }
        ConfigInfo info = getConfig(type);
        if (info != null) {
            return info.getSlotInfo(getSide(direction));
        }
        return null;
    }

    public boolean supports(TransmissionType type) {
        return configInfo.containsKey(type);
    }

    @Override
    public void tick() {
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.getBoolean("sideDataStored")) {
            for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
                TransmissionType type = entry.getKey();
                ConfigInfo info = entry.getValue();
                info.setSideConfigOld(new SideConfig(nbtTags.getByteArray("config" + type.ordinal())));
                info.setEjecting(nbtTags.getBoolean("ejecting" + type.ordinal()));
            }
        }
    }

    @Override
    public void read(PacketBuffer dataStream) {
        //TODO: Do we actually want to clear this?
        configInfo.clear();
        int amount = dataStream.readInt();
        for (int i = 0; i < amount; i++) {
            TransmissionType type = dataStream.readEnumValue(TransmissionType.class);
            //TODO: ISideConfiguration#getOrientation?
            ConfigInfo info = new ConfigInfo(() -> tileEntity.getDirection());
            info.setEjecting(dataStream.readBoolean());
            for (RelativeSide side : EnumUtils.SIDES) {
                info.setDataType(side, dataStream.readEnumValue(DataType.class));
            }
            this.configInfo.put(type, info);
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            nbtTags.putByteArray("config" + type.ordinal(), info.getSideConfigOld().asByteArray());
            nbtTags.putBoolean("ejecting" + type.ordinal(), info.isEjecting());
        }
        nbtTags.putBoolean("sideDataStored", true);
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(configInfo.size());
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            data.add(type);
            data.add(info.isEjecting());
            for (RelativeSide side : EnumUtils.SIDES) {
                data.add(info.getDataType(side));
            }
        }
    }

    @Override
    public void invalidate() {
    }

    public boolean isEjecting(TransmissionType type) {
        ConfigInfo info = getConfig(type);
        return info != null && info.isEjecting();
    }

    public void setEjecting(TransmissionType type, boolean eject) {
        ConfigInfo info = getConfig(type);
        if (info != null) {
            info.setEjecting(eject);
            MekanismUtils.saveChunk(tileEntity);
        }
        //TODO: Else print warning/error? Or should we add it or something
    }
}