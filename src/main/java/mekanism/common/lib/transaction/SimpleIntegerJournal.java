package mekanism.common.lib.transaction;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public class SimpleIntegerJournal extends SnapshotJournal<Integer> {

    public int value;

    public SimpleIntegerJournal() {
    }

    public SimpleIntegerJournal(int value) {
        this.value = value;
    }

    @Override
    protected Integer createSnapshot() {
        return value;
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        this.value = snapshot;
    }
}