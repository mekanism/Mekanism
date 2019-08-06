package mekanism.common.tile.transmitter.logistical_transporter;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;

public class TileEntityDiversionTransporter extends TileEntityLogisticalTransporter {

    public int[] modes = {0, 0, 0, 0, 0, 0};

    public TileEntityDiversionTransporter() {
        super(MekanismBlock.DIVERSION_TRANSPORTER);
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.DIVERSION_TRANSPORTER;
    }

    @Override
    public void readFromNBT(CompoundNBT nbtTags) {
        super.readFromNBT(nbtTags);
        if (nbtTags.hasKey("modes")) {
            modes = nbtTags.getIntArray("modes");
        }
    }

    @Nonnull
    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setIntArray("modes", modes);
        return nbtTags;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) throws Exception {
        super.handlePacketData(dataStream);
        if (getWorld().isRemote) {
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
        data = super.getNetworkedData(data);
        return addModes(data);
    }

    @Override
    public TileNetworkList makeSyncPacket(int stackId, TransporterStack stack) {
        return addModes(super.makeSyncPacket(stackId, stack));
    }

    @Override
    public TileNetworkList makeBatchPacket(Map<Integer, TransporterStack> updates, Set<Integer> deletes) {
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
        String description = "ERROR";
        modes[side.ordinal()] = newMode;
        switch (newMode) {
            case 0:
                description = LangUtils.localize("control.disabled.desc");
                break;
            case 1:
                description = LangUtils.localize("control.high.desc");
                break;
            case 2:
                description = LangUtils.localize("control.low.desc");
                break;
        }
        refreshConnections();
        notifyTileChange();
        player.sendMessage(new StringTextComponent(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " " +
                                                   LangUtils.localize("tooltip.configurator.toggleDiverter") + ": " + EnumColor.RED + description));
        Mekanism.packetHandler.sendUpdatePacket(this);
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean canConnect(Direction side) {
        if (!super.canConnect(side)) {
            return false;
        }
        int mode = modes[side.ordinal()];
        boolean redstone = MekanismUtils.isGettingPowered(getWorld(), new Coord4D(getPos(), getWorld()));
        return (mode != 2 || !redstone) && (mode != 1 || redstone);
    }

    @Override
    public EnumColor getRenderColor() {
        return null;
    }
}