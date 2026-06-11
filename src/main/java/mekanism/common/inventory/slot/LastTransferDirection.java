package mekanism.common.inventory.slot;

import com.mojang.serialization.Codec;
import java.util.Locale;
import mekanism.api.SerializationConstants;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public enum LastTransferDirection implements StringRepresentable {
    UNKNOWN,
    FILL_FROM_ITEM,
    DRAIN_INTO_ITEM;

    public static final Codec<LastTransferDirection> CODEC = StringRepresentable.fromEnum(LastTransferDirection::values);

    private final String serializedName;

    LastTransferDirection() {
        this.serializedName = name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static class LastDirectionJournal extends SnapshotJournal<LastTransferDirection> implements ValueIOSerializable {

        private LastTransferDirection direction = UNKNOWN;

        public void updateDirection(LastTransferDirection direction, @Nullable TransactionContext transaction) {
            if (transaction != null) {
                updateSnapshots(transaction);
            }
            this.direction = direction;
        }

        public LastTransferDirection getDirection() {
            return direction;
        }

        @Override
        protected LastTransferDirection createSnapshot() {
            return direction;
        }

        @Override
        protected void revertToSnapshot(LastTransferDirection snapshot) {
            direction = snapshot;
        }

        @Override
        public void serialize(ValueOutput output) {
            if (direction != UNKNOWN) {
                output.store(SerializationConstants.LAST_TRANSFER_DIRECTION, CODEC, direction);
            }
        }

        @Override
        public void deserialize(ValueInput input) {
            direction = input.read(SerializationConstants.LAST_TRANSFER_DIRECTION, CODEC).orElse(UNKNOWN);
        }
    }
}