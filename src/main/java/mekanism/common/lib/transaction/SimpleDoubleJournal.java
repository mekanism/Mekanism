package mekanism.common.lib.transaction;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public class SimpleDoubleJournal extends SnapshotJournal<Double> {

    public double value;

    public SimpleDoubleJournal() {
    }

    public SimpleDoubleJournal(double value) {
        this.value = value;
    }

    @Override
    protected Double createSnapshot() {
        return value;
    }

    @Override
    protected void revertToSnapshot(Double snapshot) {
        this.value = snapshot;
    }
}