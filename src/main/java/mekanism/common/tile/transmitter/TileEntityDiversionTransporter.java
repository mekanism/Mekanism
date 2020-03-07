package mekanism.common.tile.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.AlloyTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

public class TileEntityDiversionTransporter extends TileEntityLogisticalTransporter {

    public int[] modes = {0, 0, 0, 0, 0, 0};

    public TileEntityDiversionTransporter() {
        super(MekanismBlocks.DIVERSION_TRANSPORTER);
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.DIVERSION_TRANSPORTER;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setIntArrayIfPresent(nbtTags, NBTConstants.MODES, modes -> this.modes = modes);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putIntArray(NBTConstants.MODES, modes);
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) throws Exception {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            modes[0] = dataStream.readInt();
            modes[1] = dataStream.readInt();
            modes[2] = dataStream.readInt();
            modes[3] = dataStream.readInt();
            modes[4] = dataStream.readInt();
            modes[5] = dataStream.readInt();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        return addModes(super.getNetworkedData(data));
    }

    @Override
    public TileNetworkList makeSyncPacket(int stackId, TransporterStack stack) {
        return addModes(super.makeSyncPacket(stackId, stack));
    }

    @Override
    public TileNetworkList makeBatchPacket(Int2ObjectMap<TransporterStack> updates, IntSet deletes) {
        return addModes(super.makeBatchPacket(updates, deletes));
    }

    private TileNetworkList addModes(TileNetworkList data) {
        data.add(modes[0]);
        data.add(modes[1]);
        data.add(modes[2]);
        data.add(modes[3]);
        data.add(modes[4]);
        data.add(modes[5]);
        return data;
    }

    @Override
    protected ActionResultType onConfigure(PlayerEntity player, int part, Direction side) {
        int newMode = (modes[side.ordinal()] + 1) % 3;
        ILangEntry langEntry;
        modes[side.ordinal()] = newMode;
        switch (newMode) {
            case 0:
                langEntry = MekanismLang.DIVERSION_CONTROL_DISABLED;
                break;
            case 1:
                langEntry = MekanismLang.DIVERSION_CONTROL_HIGH;
                break;
            case 2:
                langEntry = MekanismLang.DIVERSION_CONTROL_LOW;
                break;
            default:
                langEntry = MekanismLang.NONE;
                break;
        }
        refreshConnections();
        notifyTileChange();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
              MekanismLang.TOGGLE_DIVERTER.translateColored(EnumColor.GRAY, EnumColor.RED, langEntry)));
        Mekanism.packetHandler.sendUpdatePacket(this);
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean canConnect(Direction side) {
        if (!super.canConnect(side)) {
            return false;
        }
        int mode = modes[side.ordinal()];
        boolean redstone = MekanismUtils.isGettingPowered(getWorld(), getPos());
        return (mode != 2 || !redstone) && (mode != 1 || redstone);
    }

    @Override
    public EnumColor getRenderColor() {
        return null;
    }

    @Nonnull
    @Override
    protected TransmitterModelData.Diversion initModelData() {
        return new TransmitterModelData.Diversion();
    }

    @Override
    protected boolean canUpgrade(AlloyTier tier) {
        return false;
    }
}