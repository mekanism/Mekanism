package mekanism.common.content.network.transmitter;

import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;

public class DiversionTransporter extends LogisticalTransporterBase {

    public final DiversionControl[] modes;

    public DiversionTransporter(TileEntityTransmitter tile) {
        super(tile, TransporterTier.BASIC);
        modes = new DiversionControl[EnumUtils.DIRECTIONS.length];
        Arrays.fill(modes, DiversionControl.DISABLED);
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

    private void readModes(@Nonnull CompoundNBT tag) {
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setEnumIfPresent(tag, NBTConstants.MODE + index, DiversionControl::byIndexStatic, mode -> modes[index] = mode);
        }
    }

    @Nonnull
    private CompoundNBT writeModes(@Nonnull CompoundNBT nbtTags) {
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            nbtTags.putInt(NBTConstants.MODE + i, modes[i].ordinal());
        }
        return nbtTags;
    }

    @Override
    public void read(@Nonnull CompoundNBT nbtTags) {
        super.read(nbtTags);
        readModes(nbtTags);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        return writeModes(super.write(nbtTags));
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag(CompoundNBT updateTag) {
        return writeModes(super.getReducedUpdateTag(updateTag));
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        readModes(tag);
    }

    @Override
    public ActionResultType onConfigure(PlayerEntity player, Direction side) {
        int index = side.ordinal();
        DiversionControl newMode = modes[index].getNext();
        modes[index] = newMode;
        refreshConnections();
        notifyTileChange();
        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY,
              MekanismLang.TOGGLE_DIVERTER.translate(EnumColor.RED, newMode)), Util.DUMMY_UUID);
        getTransmitterTile().sendUpdatePacket();
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean canConnect(Direction side) {
        if (super.canConnect(side)) {
            DiversionControl mode = modes[side.ordinal()];
            if (mode == DiversionControl.HIGH) {
                return isGettingPowered();
            } else if (mode == DiversionControl.LOW) {
                return !isGettingPowered();
            }
            return true;
        }
        return false;
    }

    private boolean isGettingPowered() {
        return WorldUtils.isGettingPowered(getTileWorld(), getTilePos());
    }

    public enum DiversionControl implements IIncrementalEnum<DiversionControl>, IHasTextComponent {
        DISABLED(MekanismLang.DIVERSION_CONTROL_DISABLED),
        HIGH(MekanismLang.DIVERSION_CONTROL_HIGH),
        LOW(MekanismLang.DIVERSION_CONTROL_LOW);

        private static final DiversionControl[] MODES = values();
        private final ILangEntry langEntry;

        DiversionControl(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translate();
        }

        @Nonnull
        @Override
        public DiversionControl byIndex(int index) {
            return byIndexStatic(index);
        }

        public static DiversionControl byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}