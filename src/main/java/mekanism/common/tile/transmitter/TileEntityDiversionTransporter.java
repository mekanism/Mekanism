package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.tier.AlloyTier;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.MekanismLang;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
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
    public void onNeighborBlockChange(Direction side) {
        //Override onNeighborBlockChange to recheck all connections as our connections
        // might have changed due to redstone
        byte current = getAllCurrentConnections();
        refreshConnections();
        if (current != getAllCurrentConnections()) {
            //Has to be markDirtyTransmitters instead of notify tile change
            // or it will not properly tell the neighboring connections that
            // it is no longer valid
            markDirtyTransmitters();
        }
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

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            updateTag.putInt(NBTConstants.MODE + i, modes[i]);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setIntIfPresent(tag, NBTConstants.MODE + index, mode -> modes[index] = mode);
        }
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
        sendUpdatePacket();
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