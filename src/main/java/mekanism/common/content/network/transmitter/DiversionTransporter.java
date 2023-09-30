package mekanism.common.content.network.transmitter;

import java.util.Arrays;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiversionTransporter extends LogisticalTransporterBase {

    public final DiversionControl[] modes;
    @Nullable
    private Boolean wasGettingPower;

    public DiversionTransporter(TileEntityTransmitter tile) {
        super(tile, TransporterTier.BASIC);
        modes = new DiversionControl[EnumUtils.DIRECTIONS.length];
        Arrays.fill(modes, DiversionControl.DISABLED);
    }

    @Override
    public void onNeighborBlockChange(Direction side) {
        //Override onNeighborBlockChange to recheck all connections as our connections
        // might have changed due to the redstone mode changing
        boolean receivingPower = isGettingPowered();
        //Lazy init our wasGetting power. In theory that may mean we check when going to the same state for the first go around
        // but at least it will reduce the number of times we check
        //TODO - 1.20: Evaluate storing it in nbt instead so it can persist and we can do it more accurately without worrying about
        // not properly invalidating pre-existing ones
        if (wasGettingPower == null || wasGettingPower != receivingPower) {
            wasGettingPower = receivingPower;
            byte current = getAllCurrentConnections();
            refreshConnections();
            if (current != getAllCurrentConnections()) {
                //Has to be markDirtyTransmitters instead of notify tile change,
                // or it will not properly tell the neighboring connections that
                // it is no longer valid
                markDirtyTransmitters();
            }

            //If the mode reqs being met changed, we need to update our stored value and update if there is a cap exposed
            TileEntityTransmitter transmitterTile = getTransmitterTile();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                if (super.exposesInsertCap(direction)) {
                    if (!modeReqsMet(direction)) {
                        transmitterTile.invalidateCapability(ForgeCapabilities.ITEM_HANDLER, direction);
                    }
                    WorldUtils.notifyNeighborOfChange(transmitterTile.getLevel(), direction, transmitterTile.getTilePos());
                }
            }
        }
    }

    private void readModes(@NotNull CompoundTag tag) {
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            int index = i;
            NBTUtils.setEnumIfPresent(tag, NBTConstants.MODE + index, DiversionControl::byIndexStatic, mode -> modes[index] = mode);
        }
    }

    @NotNull
    private CompoundTag writeModes(@NotNull CompoundTag nbtTags) {
        for (int i = 0; i < EnumUtils.DIRECTIONS.length; i++) {
            NBTUtils.writeEnum(nbtTags, NBTConstants.MODE + i, modes[i]);
        }
        return nbtTags;
    }

    @Override
    public void read(@NotNull CompoundTag nbtTags) {
        super.read(nbtTags);
        readModes(nbtTags);
    }

    @NotNull
    @Override
    public CompoundTag write(@NotNull CompoundTag nbtTags) {
        return writeModes(super.write(nbtTags));
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(CompoundTag updateTag) {
        return writeModes(super.getReducedUpdateTag(updateTag));
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag);
        readModes(tag);
    }

    public void updateMode(Direction side, DiversionControl mode) {
        int ordinal = side.ordinal();
        DiversionControl oldMode = modes[ordinal];
        if (oldMode != mode) {
            modes[ordinal] = mode;
            TileEntityTransmitter transmitterTile = getTransmitterTile();
            if (super.exposesInsertCap(side)) {
                //If our super impl would expose a cap see if our overrides that just changed also changed if we should provide it
                boolean nowExposes = modeReqsMet(mode);
                if (nowExposes != modeReqsMet(oldMode)) {
                    //If the only thing that changed whether the cap should be exposed is the mode we need to invalidate the cap
                    if (!nowExposes) {
                        transmitterTile.invalidateCapability(ForgeCapabilities.ITEM_HANDLER, side);
                    }
                    WorldUtils.notifyNeighborOfChange(transmitterTile.getLevel(), side, transmitterTile.getTilePos());
                }
            }
            refreshConnections();
            notifyTileChange();
            transmitterTile.sendUpdatePacket();
        }
    }

    @Override
    public InteractionResult onRightClick(Player player, Direction side) {
        side = getTransmitterTile().getSideLookingAt(player, side);
        DiversionControl newMode = modes[side.ordinal()].getNext();
        updateMode(side, newMode);
        player.displayClientMessage(MekanismLang.TOGGLE_DIVERTER.translateColored(EnumColor.GRAY, EnumColor.RED, newMode), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean exposesInsertCap(@NotNull Direction side) {
        return super.exposesInsertCap(side) && modeReqsMet(side);
    }

    @Override
    public boolean canConnect(Direction side) {
        return super.canConnect(side) && modeReqsMet(side);
    }

    private boolean modeReqsMet(Direction side) {
        return modeReqsMet(modes[side.ordinal()]);
    }

    private boolean modeReqsMet(DiversionControl mode) {
        //TODO: Eventually it might be nice to make this use wasGettingPowered so as to avoid looking at the world when checking the mode reqs
        // as this might provide a decent boost to diversion transporter performance
        return switch (mode) {
            case HIGH -> isGettingPowered();
            case LOW -> !isGettingPowered();
            default -> true;
        };
    }

    private boolean isGettingPowered() {
        return WorldUtils.isGettingPowered(getTileWorld(), getTilePos());
    }

    @NothingNullByDefault
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

        @Override
        public DiversionControl byIndex(int index) {
            return byIndexStatic(index);
        }

        public static DiversionControl byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}