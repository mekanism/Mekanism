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
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

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
            //Has to be markDirtyTransmitters instead of notify tile change,
            // or it will not properly tell the neighboring connections that
            // it is no longer valid
            markDirtyTransmitters();
        }
    }

    private void readModes(@Nonnull CompoundTag tag) {
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setEnumIfPresent(tag, NBTConstants.MODE + index, DiversionControl::byIndexStatic, mode -> modes[index] = mode);
        }
    }

    @Nonnull
    private CompoundTag writeModes(@Nonnull CompoundTag nbtTags) {
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            NBTUtils.writeEnum(nbtTags, NBTConstants.MODE + i, modes[i]);
        }
        return nbtTags;
    }

    @Override
    public void read(@Nonnull CompoundTag nbtTags) {
        super.read(nbtTags);
        readModes(nbtTags);
    }

    @Nonnull
    @Override
    public CompoundTag write(@Nonnull CompoundTag nbtTags) {
        return writeModes(super.write(nbtTags));
    }

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag(CompoundTag updateTag) {
        return writeModes(super.getReducedUpdateTag(updateTag));
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        readModes(tag);
    }

    public void updateMode(Direction side, DiversionControl mode) {
        int ordinal = side.ordinal();
        if (modes[ordinal] != mode) {
            modes[ordinal] = mode;
            refreshConnections();
            notifyTileChange();
            getTransmitterTile().sendUpdatePacket();
        }
    }

    @Override
    public InteractionResult onRightClick(Player player, Direction side) {
        side = getTransmitterTile().getSideLookingAt(player, side);
        DiversionControl newMode = modes[side.ordinal()].getNext();
        updateMode(side, newMode);
        player.sendMessage(MekanismUtils.logFormat(MekanismLang.TOGGLE_DIVERTER.translate(EnumColor.RED, newMode)), Util.NIL_UUID);
        return InteractionResult.SUCCESS;
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
        public Component getTextComponent() {
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