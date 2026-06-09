package mekanism.common.content.network.transmitter;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

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
    public void onNeighborBlockChange(@Nullable Direction side) {
        //Override onNeighborBlockChange to recheck all connections as our connections
        // might have changed due to the redstone mode changing
        boolean receivingPower = isGettingPowered();
        //Lazy init our wasGetting power. In theory that may mean we check when going to the same state for the first go around
        // but at least it will reduce the number of times we check
        //TODO - 1.21: Evaluate storing it in nbt instead so it can persist and we can do it more accurately without worrying about
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
                    if (modeReqsMet(direction)) {
                        //Notify any listeners to our position that we now do have a capability
                        //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
                        transmitterTile.invalidateCapabilities();
                    } else {
                        //We no longer have a capability, invalidate it, which will also notify the level
                        transmitterTile.invalidateCapability(Capabilities.ITEM.block(), direction);
                    }
                }
            }
        } else {
            //If we can't handle redstone, just refresh the side the connection changed on so that if a transmitter is removed
            // or is set to none then we stop trying to be connected to it
            //TODO - 1.20.2: See if we can come up with a better way to handle this as there are definitely better ways
            if (side == null) {//TODO - 26.1: Re-evaluate
                refreshConnections();
            } else {
                refreshConnections(side);
            }
        }
    }

    private void readModes(ValueInput input) {
        Optional<int[]> optionalModes = input.getIntArray(SerializationConstants.MODE);
        if (optionalModes.isPresent()) {
            int[] modeIndices = optionalModes.get();
            for (int i = 0; i < modeIndices.length && i < modes.length; i++) {
                modes[i] = DiversionControl.BY_ID.apply(modeIndices[i]);
            }
        }
    }

    private void writeModes(ValueOutput output) {
        int[] modeIndices = new int[modes.length];
        for (int i = 0; i < modes.length; i++) {
            modeIndices[i] = modes[i].ordinal();
        }
        output.putIntArray(SerializationConstants.MODE, modeIndices);
    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        readModes(input);
    }

    @Override
    public void write(ValueOutput output) {
        super.write(output);
        writeModes(output);
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        writeModes(output);
    }

    @Override
    public boolean handleUpdateTag(ValueInput input) {
        boolean refreshModelData = super.handleUpdateTag(input);
        readModes(input);
        return refreshModelData;
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
                    if (nowExposes) {
                        //Notify any listeners to our position that we now do have a capability
                        //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
                        transmitterTile.invalidateCapabilities();
                    } else {
                        //We no longer have a capability, invalidate it, which will also notify the level
                        transmitterTile.invalidateCapability(Capabilities.ITEM.block(), side);
                    }
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
        player.sendOverlayMessage(MekanismLang.TOGGLE_DIVERTER.translateColored(EnumColor.GRAY, EnumColor.RED, newMode));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean exposesInsertCap(Direction side) {
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
        return WorldUtils.isGettingPowered(getLevel(), getBlockPos());
    }

    public enum DiversionControl implements IIncrementalEnum<DiversionControl>, IHasEnumNameTextComponent {
        DISABLED(MekanismLang.DIVERSION_CONTROL_DISABLED),
        HIGH(MekanismLang.DIVERSION_CONTROL_HIGH),
        LOW(MekanismLang.DIVERSION_CONTROL_LOW);

        public static final IntFunction<DiversionControl> BY_ID = ByIdMap.continuous(DiversionControl::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, DiversionControl> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DiversionControl::ordinal);

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
            return BY_ID.apply(index);
        }
    }
}