package mekanism.common.lib.transaction;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public class SimpleLongJournal extends SnapshotJournal<Long> {

    public long value;

    public SimpleLongJournal() {
    }

    public SimpleLongJournal(long value) {
        this.value = value;
    }

    @Override
    protected Long createSnapshot() {
        return value;
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {
        this.value = snapshot;
    }
}